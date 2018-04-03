package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{CarReserveModelPlotInfo, ClassColorEnum, exCloudBtxData}
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.silhouette.MyEnv


/**
  * 高所作業車予約アクションクラス
  *
  *
  */
@Singleton
class CarReserve @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , ws: WSClient
                           , carReserveDAO: models.carReserveDAO
                           , reserveDAO: models.reserveDAO
                           , floorDAO: models.floorDAO
                           , placeDAO: models.placeDAO
                           , carDAO: models.carDAO
                           , companyDAO: models.companyDAO
                               ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction.async { implicit request =>
    // 現場情報
    val placeId = super.getCurrentPlaceId
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last

    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 業者情報
    val companyList = companyDAO.selectCompany(placeId)
    // 作業車情報
    val carList = carDAO.selectCarInfo(placeId)
    // 予約情報
    val reserveInfoList = carReserveDAO.selectReserveForPlot(placeId, new DateTime)
    // 前日の予約情報
    val yesterdayReserveList = reserveDAO.selectReserve(placeId, floorInfoList.map{f => f.floorId}, new DateTime().minusDays(1).toString("yyyyMMdd"))
    // 稼働情報
    var workList = Seq[CarReserveModelPlotInfo]()

    // API呼び出し
    ws.url(place.btxApiUrl).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // 稼働情報
      floorInfoList.foreach { floor => // -- ループ start --
        // 実際の作業車Tx
        val carsAtFloor = list
          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
          .filter(carList.map{c => c.carBtxId} contains _.btx_id)  // 予約作業車のBTXidに合致するもの

        carsAtFloor.foreach{carBtx =>
            val rest = carList.filter(_.carKeyBtxId == carBtx.btx_id)
            if(rest.isEmpty == false) {
              val c = rest.last
              workList :+= CarReserveModelPlotInfo(
                floor.floorId.toString
                , companyIdStr = {
                  val ddd = yesterdayReserveList.filter(_.floorId == floor.floorId).filter(_.carId == c.carId)
                  if (ddd.isEmpty == false) {
                    ddd.last.companyId.toString
                  } else {
                    ""
                  }
                }
                , c.carId.toString
                , c.carNo
                , colorName = {
                  if(reserveInfoList.filter(_.carIdStr == c.carId.toString).isEmpty==false){
                    ClassColorEnum().RESERVE_DONE
                  }else {
                    ClassColorEnum().RESERVE_NONE
                  }
                }
              )
            }
          }
      }



      Ok(views.html.carReserve(companyList, floorInfoList, reserveInfoList, workList))
    }


  }

}
