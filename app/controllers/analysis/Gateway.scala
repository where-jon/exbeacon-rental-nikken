package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc.Action
import utils.silhouette.MyEnv
import scala.concurrent.ExecutionContext.Implicits._

/**
  * GW状態監視
  *
  *
  */
@Singleton
class Gateway @Inject()(config: Configuration
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
      val placeId = super.getCurrentPlaceId
      // 非稼働時間かどうか
      val isNoWorkTime = (new DateTime().toString("HHmm") < config.getString("noWorkTimeEnd").get)
      Ok(views.html.analysis.gateway(isNoWorkTime))
    }else{
      Redirect(site.routes.WorkPlace.index)
    }
  }
}
