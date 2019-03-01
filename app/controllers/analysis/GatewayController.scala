package controllers.analysis

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors, site}
import play.api._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws._
import services.analysis.TelemetryService
import utils.silhouette.MyEnv

/**
  * GW状態監視
  *
  *
  */
@Singleton
class GatewayController @Inject()(
   config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , beaconService: BeaconService
  , telemetryService: TelemetryService
  , ws: WSClient
  , exbDao:models.system.ExbDAO
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
      if(beaconService.getCloudUrl(placeId)){
        val gwListApi = telemetryService.getGatewayState(placeId)
        if(gwListApi!=null){
          Ok(views.html.analysis.gateway(gwListApi))
        }else{
          // apiと登録データが違う場合
          Redirect(errors.routes.UnDetectedApi.indexAnalysis)
            .flashing(ERROR_MSG_KEY -> Messages("error.unmatched.data"))
        }
      }else{
        // apiデータがない場合
        Redirect(errors.routes.UnDetectedApi.indexAnalysis)
          .flashing(ERROR_MSG_KEY -> Messages("error.undetected.api"))
      }
    }else{
      Redirect(site.routes.ItemCarListController.index)
    }
  }
}
