package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{OtherItemInfo, _}
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * その他仮設材利用状況アクションクラス
  *
  *
  */
@Singleton
class OtherItem @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , ws: WSClient
                          , otherItemDAO: models.otherItemDAO
                          , placeDAO: models.placeDAO
                          , floorDAO: models.manage.floorDAO
                          , btxLastPositionDAO: models.btxLastPositionDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 現場ID
    val placeId = super.getCurrentPlaceId
    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)

    // フロア情報
    var floorId = 0
    if (request.getQueryString("floorId").isEmpty == false) {
      floorId = request.getQueryString("floorId").get.toInt
    }else{
      if(floorInfoList.nonEmpty){
        floorId = floorInfoList(0).floorId
      }
    }

    // DBデータ
    val itemMap = otherItemDAO.selectItemMap(placeId)

    Ok(views.html.otherItem(itemMap, floorId))
  }

  /**
    * 仮設材位置情報のJSON出力
    *
    * @return
    */
  def getPlotInfo = SecuredAction.async { implicit request =>
    // 結果
    var otherItemInfoList = Seq[OtherItemInfo]()

    // 現場ID
    val placeId = super.getCurrentPlaceId
    // 建築現場情報
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId = placeId)
    // 仮設材情報
    val itemDbList = otherItemDAO.selectItemInfo(placeId)
    // 履歴input
    var inputPosition = Seq[BtxLastPosition]()

    // パラメータ
    var paramFloorId = ""
    if (request.getQueryString("floorId").isEmpty == false) {
      paramFloorId = request.getQueryString("floorId").get
    }else{
      paramFloorId = floorInfoList(0).floorId.toString
    }

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>
      // APIデータ
      var list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)
      // 中身を仮設材のBTXのもののみにする
      list = list.filter(itemDbList.map{i=>i.itemBtxId} contains _.btx_id)

      list.foreach { apiData => // -- foreach start --
        var itemNo: String = ""
        var itemKindId: Int = 0
        var itemKindName: String = ""
        var itemBtxId: Int = 0
        var floorIdStr: String = ""

        val item = itemDbList.filter(_.itemBtxId == apiData.btx_id)
        if(item.nonEmpty){
          // 仮設材情報
          itemNo = item.last.itemNo
          itemKindId = item.last.itemKindId
          itemKindName = item.last.itemKindName
          itemBtxId = item.last.itemBtxId

          // フロア決定
          val nearestFloors = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
          if(nearestFloors.nonEmpty){
            floorIdStr = nearestFloors.last.floorId.toString
          }else{
            // 履歴DBから取得
            val hist = btxLastPositionDAO.find(placeId, Seq[Int](itemBtxId))
            if(hist.nonEmpty){
              floorIdStr = hist.last.floorId.toString
            }else{
              // 表示なし
              Logger.warn(s"履歴が無いため表示なし。現場ID = ${placeId}, 仮設材管理No = ${itemNo}, btx_id = ${itemBtxId}")
            }
          }

          // 値のセット
          otherItemInfoList :+= OtherItemInfo(itemNo, itemKindId, itemKindName, itemBtxId, floorIdStr)

        }else{
          // 仮設材としてのDB未登録
          Logger.debug(s"""仮設材としてのDB未登録。btx_id：${apiData.btx_id}""")
        }

        // 履歴のインプットを貯める
        val floors = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
        if(floors.nonEmpty){
          inputPosition :+= BtxLastPosition(apiData.btx_id, placeId, floors.last.floorId)
        }
      }// -- foreach end --

      // 履歴の登録
      btxLastPositionDAO.update(inputPosition)

      // 集計 ------------------------
      var allCount = 0
      val otherItemSummeryInfoList = floorInfoList.map { f =>
        val count = otherItemInfoList.filter(_.floorIdStr == f.floorId.toString).length
        allCount += count
        OtherItemSummeryInfo(f.floorId, f.floorName, count)
      }

      // 最終結果
      val result = OtherItemPlotInfo(
        otherItemInfoList.filter(_.floorIdStr == paramFloorId).toList
        , otherItemSummeryInfoList.toList
        , allCount
      )

      Ok(Json.toJson(result))
    }
  }
}