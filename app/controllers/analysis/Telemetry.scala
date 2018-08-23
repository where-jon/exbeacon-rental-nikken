package controllers.analysis

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, site}
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws._
import utils.silhouette.MyEnv

/**
  * EXB状態監視
  *
  *
  */
@Singleton
class Telemetry @Inject()(
   config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , beaconService: BeaconService
  , ws: WSClient
  , exbDao:models.ExbDAO
  , btxLastPositionDAO: models.btxLastPositionDAO
  ) extends BaseController with I18nSupport {


  var placeId : Int = -1
  /* 仮設材テーブルと予約テーブルとapiを結合したデータを取得*/
  def getData = SecuredAction{ implicit request =>
    val placeId = super.getCurrentPlaceId
    val gatewayListApi = beaconService.getTelemetryState(placeId)
    Ok(Json.toJson(gatewayListApi))

  }

    /**
    * 初期表示
    * @return
    */
  def index = SecuredAction { implicit request =>
     placeId = super.getCurrentPlaceId
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      val placeId = super.getCurrentPlaceId
      val exbListApi = beaconService.getTelemetryState(placeId)
      Ok(views.html.analysis.telemetry(exbListApi))

    }else{
      Redirect(site.routes.WorkPlace.index)
    }
  }
}
