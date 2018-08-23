package controllers.analysis

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, site}
import org.joda.time.DateTime
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws._
import utils.silhouette.MyEnv

/**
  * GW状態監視
  *
  *
  */
@Singleton
class Telemetry @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , beaconService: BeaconService
  , ws: WSClient
  , exbDao:models.ExbDAO
  , btxLastPositionDAO: models.btxLastPositionDAO
  ) extends BaseController with I18nSupport {



  /* 仮設材テーブルと予約テーブルとapiを結合したデータを取得*/
  def getData = SecuredAction{ implicit request =>
    val placeId = super.getCurrentPlaceId
    val gatewayListApi = beaconService.getGateWayState(placeId)
    Ok(Json.toJson(gatewayListApi))
  }


    /**
    * 初期表示
    * @return
    */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      Ok(views.html.analysis.telemetry())
    }else{
      Redirect(site.routes.WorkPlace.index)
    }
  }
}
