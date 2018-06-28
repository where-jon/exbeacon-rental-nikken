package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.{Car, CarData}
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
                          , carDAO: models.carDAO
                          , btxDAO: models.btxDAO
                         ) extends BaseController with I18nSupport {


  var FILTER1 = 0

  val carForm = Form(mapping(
    "placeId" -> number
  )(CarData.apply)(CarData.unapply))

  /**　検索ロジック*/
  def search = SecuredAction { implicit request =>
    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------start updateExbViewer:");
    // 部署情報
    var carFormData = carForm.bindFromRequest.get;
    var vPlaceId = carFormData.placeId
    FILTER1 = vPlaceId

    var carList: Seq[Car] = null

    if(vPlaceId!=0){
      carList = carDAO.selectCarMasterInfo(vPlaceId)

    }else{
      carList = carDAO.selectCarMasterAll()
    }
    Ok(views.html.site.carMaster(FILTER1,carList))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if (super.isCmsLogged) {

      val carList = carDAO.selectCarMasterAll()
      Ok(views.html.site.carMaster(FILTER1,carList))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
