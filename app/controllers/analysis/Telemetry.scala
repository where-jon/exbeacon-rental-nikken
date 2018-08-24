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

    /**
    * 初期表示
    * @return
    */
  def index = SecuredAction { implicit request =>
    val  placeId = super.getCurrentPlaceId
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      val placeId = super.getCurrentPlaceId
      val exbListApi = beaconService.getTelemetryState(placeId)
      if(exbListApi!=null){
        Ok(views.html.analysis.telemetry(exbListApi))
      }else{
        // apiデータがない場合
        Redirect(site.routes.UnDetected.index)
      }
    }else{
      Redirect(site.routes.WorkPlace.index)
    }
  }
}
