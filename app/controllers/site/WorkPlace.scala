package controllers.site

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
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
  , exbDAO: models.manage.ExbDAO
  , floorDAO: models.manage.floorDAO
  , itemTypeDAO: models.ItemTypeDAO
  ) extends BaseController with I18nSupport {


  /* 仮設材テーブルと予約テーブルとapiを結合したデータを取得*/
  def getData = SecuredAction{ implicit request =>
    val placeId = super.getCurrentPlaceId
    // ①仮設材すべてを取得
    val dbDatas = beaconDAO.selectBeaconViewer(placeId)
    // ②仮設材種別作業車の鍵をものとして取得
    val dbDatasKeyData = beaconDAO.selectCarKeyViewer(placeId)
    // ①、②を結合
    val totalDbDatas = dbDatas union  dbDatasKeyData
    val beaconListApi = beaconService.getBeaconPosition(totalDbDatas,true,placeId)

    Ok(Json.toJson(beaconListApi))
  }


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val placeId = super.getCurrentPlaceId
    if(beaconService.getCloudUrl(placeId)){
      val UPDATE_SEC = config.getInt("web.positioning.updateSec").get
      val VIEW_COUNT = config.getInt("web.positioning.viewCount").get

      /*仮設材種別取得*/
      val itemTypeList = itemTypeDAO.selectItemTypeInfo(placeId);
      // map情報
//      val mapViewer = floorDAO.selectFloorAll(placeId)
      val mapViewer = floorDAO.selectFloorValidData(placeId)
      val exbData = exbDAO.selectExbAll(placeId)

      // ①仮設材すべてを取得
      val dbDatas = beaconDAO.selectBeaconViewer(placeId)
      // ②仮設材種別作業車の鍵をものとして取得
      val dbDatasKeyData = beaconDAO.selectCarKeyViewer(placeId)
      // ①、②を結合
      val totalDbDatas = dbDatas union  dbDatasKeyData
      val beaconListApi = beaconService.getBeaconPosition(totalDbDatas,true,placeId)
      if(beaconListApi!= null) {
        Ok(views.html.site.workPlace(itemTypeList,mapViewer,exbData,UPDATE_SEC,VIEW_COUNT))
      }else{
        // apiと登録データが違う場合
        Redirect(errors.routes.UnDetectedApi.indexSite)
          .flashing(ERROR_MSG_KEY -> Messages("error.unmatched.data"))
      }
    }else{
      // apiデータがない場合
      Redirect(errors.routes.UnDetectedApi.indexSite)
        .flashing(ERROR_MSG_KEY -> Messages("error.undetected.api"))
    }
  }

}
