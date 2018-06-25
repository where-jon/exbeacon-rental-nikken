package controllers.site

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * マップ登録クラス.
  */
class WorkPlace @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , carDAO: models.carDAO
                          , ws: WSClient
                          , btxDAO: models.btxDAO
                          , mapViewerDAO: models.MapViewerDAO
                          ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>

    if (super.isCmsLogged) {
      val mapViewer = mapViewerDAO.selectAll()
      Ok(views.html.site.workPlace( mapViewer))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
