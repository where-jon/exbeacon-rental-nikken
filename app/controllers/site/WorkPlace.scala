package controllers.site

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService}
import models.itemBeaconPositionData
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * 現場状況クラス.
  */
class WorkPlace @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , beaconDAO: models.beaconDAO
                          , beaconService: BeaconService
                          , ws: WSClient
                          , exbDAO: models.ExbDAO
                          , mapViewerDAO: models.MapViewerDAO
                         ) extends BaseController with I18nSupport {

  var beaconList :Seq[itemBeaconPositionData]= null;

  /* 仮設材テーブルと予約テーブルとapiを結合したデータを取得*/
  def getData = SecuredAction{ implicit request =>
    val placeId = super.getCurrentPlaceId
    val dbDatas = beaconDAO.selectBeaconViewer(placeId)
    val beaconListApi = beaconService.getBeaconPosition(dbDatas,true,placeId)

    Ok(Json.toJson(beaconListApi))
  }


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val placeId = super.getCurrentPlaceId
    val mapViewer = mapViewerDAO.selectAll()
    val exbData = exbDAO.selectExbAll(placeId)
    Ok(views.html.site.workPlace(mapViewer,exbData))
  }

}
