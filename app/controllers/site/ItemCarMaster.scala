package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.{ItemCarData, WorkTypeEnum}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 作業車・立馬一覧クラス
  *
  *
  */


@Singleton
class ItemCarMaster @Inject()(config: Configuration
                              , val silhouette: Silhouette[MyEnv]
                              , val messagesApi: MessagesApi
                              , carDAO: models.itemCarDAO
                              , btxDAO: models.btxDAO
                              , itemTypeDAO: models.ItemTypeDAO
                             ) extends BaseController with I18nSupport {

  var ITEM_TYPE_FILTER = 0
  var WORK_TYPE_FILTER = 0
  var COMPANY_ID_FILTER = 0

  val WORK_TYPE = WorkTypeEnum().map;

  val carForm = Form(mapping(
    "itemTypeId" -> number
  )(ItemCarData.apply)(ItemCarData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    COMPANY_ID_FILTER = 0
    WORK_TYPE_FILTER = 0
  }

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    // 部署情報
    var carFormData = carForm.bindFromRequest.get
    ITEM_TYPE_FILTER = carFormData.itemTypeId
    //var carList: Seq[Car] = null
    val placeId = super.getCurrentPlaceId
    var carList = carDAO.selectCarMasterInfo(placeId)
    if (ITEM_TYPE_FILTER != 0) {
      carList = carList.filter(_.itemTypeId == ITEM_TYPE_FILTER)
    }
    val itemTypeList = itemTypeDAO.selectItemCarInfo(placeId);
    Ok(views.html.site.itemCarMaster(ITEM_TYPE_FILTER, carList,itemTypeList,WORK_TYPE))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if (super.isCmsLogged) {

      // 初期化
      init();

      val placeId = super.getCurrentPlaceId
      // dbデータ取得
      val carList = carDAO.selectCarMasterInfo(placeId)
      val itemTypeList = itemTypeDAO.selectItemCarInfo(placeId);

      //System.out.println("itemTypeList:" + itemTypeList)
      Ok(views.html.site.itemCarMaster(ITEM_TYPE_FILTER, carList,itemTypeList,WORK_TYPE))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
