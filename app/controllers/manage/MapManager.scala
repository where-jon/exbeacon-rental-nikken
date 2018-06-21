package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv

/**
  * Created by ep-146 on 2018/06/21.
  */
class MapManager @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , carDAO: models.carDAO
                           , btxDAO: models.btxDAO
                          ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>

    if (super.isCmsLogged) {
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
      val carList = carDAO.selectCarInfo(placeId = placeId)

      Ok(views.html.manage.mapManager(carList, placeId))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
