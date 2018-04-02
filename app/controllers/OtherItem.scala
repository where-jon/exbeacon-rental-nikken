package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{OtherItemInfo, OtherItemSummeryInfo, exCloudBtxData}
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
      floorId = floorInfoList(0).floorId
    }

    // DBデータ
    val itemDbList = otherItemDAO.selectItemInfo(placeId)
    val itemMap = otherItemDAO.selectItemMap(placeId)

    // API呼び出し
    ws.url(place.btxApiUrl).get().map { response =>

      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // フロア毎に処理
      var totalCount = 0
      val resultList: Seq[OtherItemSummeryInfo] = floorInfoList.map { floor => // -- ループ start --
        // 実際の仮設材Tx情報
        val itemAtFloor = list
          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
          .filter(itemDbList.map({ item => item.item_btx_id }) contains _.btx_id) // 仮設材のBTXに合致するもの

        // フロアにある数を取得
        var count = 0
        itemAtFloor.foreach(i => {
          val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
          if (rest.isEmpty == false) {
            count += 1
            totalCount += 1
          }
        })
        OtherItemSummeryInfo(floor.floorId, floor.floorName, count)
      } // -- ループ end --

      Ok(views.html.otherItem(resultList, itemMap, totalCount, floorId))
    }
  }

  /**
    * 仮設材位置情報のJSON出力
    *
    * @return
    */
  def getPlotInfo = SecuredAction.async { implicit request =>
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
    ws.url(place.btxApiUrl).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // 実際の仮設材Tx情報
      val itemAtFloor = list
        .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
        .filter(itemDbList.map({ item => item.item_btx_id }) contains _.btx_id) // 仮設材のBTXに合致するもの

      var resultList = Seq[OtherItemInfo]()
      itemAtFloor.foreach(i => { // loop start
        val rest = itemDbList.filter(_.item_btx_id == i.btx_id)
        if(rest.isEmpty == false){
          resultList :+= rest.last
        }
      })// loop end

      Ok(Json.toJson(resultList))
    }
  }
}