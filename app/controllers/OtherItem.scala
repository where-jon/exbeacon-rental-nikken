package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{BtxLastPosition, OtherItemInfo, OtherItemSummeryInfo, exCloudBtxData}
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
                          , floorDAO: models.floorDAO
                          , btxLastPositionDAO: models.btxLastPositionDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction.async { implicit request =>
    // 現場ID
    val placeId = super.getCurrentPlaceId
    // 建築現場情報
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last
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
    val itemDbList = otherItemDAO.selectItemInfo(placeId)
    val itemMap = otherItemDAO.selectItemMap(placeId)

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>

      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)
      // 総件数
      var totalCount = 0
      // フロア毎に処理
      val resultList = floorInfoList.map{ floor => // ループ - start
        // 件数
        var count = 0

        // 仮設材用のBTXを取得
        val itemsBtx = list.filter(itemDbList.map{i=>i.item_btx_id} contains _.btx_id)
        val itemDetected = itemsBtx.filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
        val itemUnDetected = itemsBtx.filter(_.device_id == 0)

        // 検知できたもの
        itemDetected.foreach(i => { // loop start
          val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
          if(rest.isEmpty == false){
            count += 1
          }
        })

        // 未検知
        itemUnDetected.foreach(i => { // loop start
          val hist = btxLastPositionDAO.find(placeId, Seq[Int](i.btx_id))
          if(hist.nonEmpty){
            if(hist.last.floorId == floor.floorId){
              val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
              if(rest.isEmpty == false){
                count += 1
              }
            }
          }
        })
        totalCount += count
        OtherItemSummeryInfo(floor.floorId, floor.floorName, count)
      }// foreach - start

      var inputPosition = Seq[BtxLastPosition]()
      list.foreach { apiData =>
        // 履歴のインプットを貯める
        val floors = floorInfoList.filter(_.exbDeviceIdList contains apiData.device_id.toString)
        if(floors.nonEmpty){
          inputPosition :+= BtxLastPosition(apiData.btx_id, placeId, floors.last.floorId)
        }
      }
      // 履歴の登録
      btxLastPositionDAO.update(inputPosition)

      Ok(views.html.otherItem(resultList, itemMap, totalCount, floorId))
    }
  }

  /**
    * 仮設材位置情報のJSON出力
    *
    * @return
    */
  def getPlotInfo = SecuredAction.async { implicit request =>
    var resultList = Seq[OtherItemInfo]()

    // 現場ID
    val placeId = super.getCurrentPlaceId
    // 建築現場情報
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    // フロア情報
    var floorIdOpt: Option[Int] = None
    if (request.getQueryString("floorId").isEmpty == false) {
      floorIdOpt = Option(request.getQueryString("floorId").get.toInt)
    }
    val floor = floorDAO.selectFloorInfo(placeId = placeId, floorId = floorIdOpt).last

    // 仮設材情報
    val itemDbList = otherItemDAO.selectItemInfo(placeId)

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // 仮設材用のBTXを取得
      val itemsBtx = list.filter(itemDbList.map{i=>i.item_btx_id} contains _.btx_id)
      val itemDetected = itemsBtx.filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
      val itemUnDetected = itemsBtx.filter(_.device_id == 0)

      // 検知できたもの
      itemDetected.foreach(i => { // loop start
        val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
        if(rest.isEmpty == false){
          resultList :+= rest.last
        }
      })// loop end

      // 未検知
      itemUnDetected.foreach(i => { // loop start
        val hist = btxLastPositionDAO.find(placeId, Seq[Int](i.btx_id))
        if(hist.nonEmpty){
          if(hist.last.floorId == floor.floorId){
            val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
            if(rest.isEmpty == false){
              resultList :+= rest.last
            }
          }
        }
      })// loop end

      Ok(Json.toJson(resultList))
    }
  }
}