package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.MapViewerData
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv
import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by ep-146 on 2018/06/21.
  */
class MapManager @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , carDAO: models.carDAO
                           , ws: WSClient
                           , btxDAO: models.btxDAO
                           , mapViewerDAO: models.MapViewerDAO
                          ) extends BaseController with I18nSupport {

    val mapViewerForm = Form(mapping(
      "map_id" -> list(number),
      "map_width" -> list(number),
      "map_height" -> list(number),
      "map_image" -> list(text),
      "map_position" -> list(text)

    )(MapViewerData.apply)(MapViewerData.unapply))

  /** 初期表示 */
  def index = SecuredAction { implicit request =>

    if (super.isCmsLogged) {
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
      val carList = carDAO.selectCarInfo(placeId = placeId)


      val mapViewer = mapViewerDAO.selectAll()
      Ok(views.html.manage.mapManager(mapViewerForm, mapViewer))
      //k(views.html.manage.mapManager(carList, placeId))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
