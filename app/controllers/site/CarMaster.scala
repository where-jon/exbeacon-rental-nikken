package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.ItemCarData
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
class CarMaster @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , carDAO: models.itemCarDAO
                          , btxDAO: models.btxDAO
                         ) extends BaseController with I18nSupport {

  var FILTER1 = 0

  val carForm = Form(mapping(
    "itemTypeId" -> number
  )(ItemCarData.apply)(ItemCarData.unapply))

  /**　初期化 */
  def init()  {
    FILTER1 = 0
  }

  /**　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    // 部署情報
    var carFormData = carForm.bindFromRequest.get
    FILTER1 = carFormData.itemTypeId
    //var carList: Seq[Car] = null
    val placeId = super.getCurrentPlaceId
    var carList = carDAO.selectCarMasterInfo(placeId)
    if(FILTER1!=0){
      carList = carList.filter(_.itemTypeId == FILTER1)
    }

    Ok(views.html.site.carMaster(FILTER1,carList))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if (super.isCmsLogged) {

      // 初期化
      init();

      val placeId = super.getCurrentPlaceId
      System.out.println("placeId:" + placeId)
      // dbデータ取得
      val carList = carDAO.selectCarMasterInfo(placeId)
      Ok(views.html.site.carMaster(FILTER1,carList))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
