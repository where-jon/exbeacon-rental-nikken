package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.ExbMasterData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * マップ登録クラス.
  */
class ExbViewerManager @Inject()(config: Configuration
                                 , val silhouette: Silhouette[MyEnv]
                                 , val messagesApi: MessagesApi
                                 , ws: WSClient
                                 , exDAO: models.ExbDAO
                                 , mapViewerDAO: models.MapViewerDAO
                                 , viewTypeDAO: models.ViewTypeDAO
                                 , exbDAO: models.ExbDAO
                          ) extends BaseController with I18nSupport {

  val exbViewerForm = Form(mapping(
    "viewerId" -> list(number),
    "viewerVisible" -> list(boolean),
    "viewerPosType" -> list(number),
    "viewerPosX" -> list(text),
    "viewerPosY" -> list(text),
    "viewerPosMargin" -> list(number),
    "viewerPosCount" -> list(number),
    "viewerPosFloor" -> list(number),
    "viewerPosSize" -> list(number),
    "viewerPosNum" -> list(number)

  )(ExbMasterData.apply)(ExbMasterData.unapply))

  def updateViewerManager = SecuredAction { implicit request =>
    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------start viewManagerControllerUpdate:");
    // 部署情報
    val placeId = super.getCurrentPlaceId
    val exbViewer = exDAO.selectExbAll(placeId)
    var exbViewerData = exbViewerForm.bindFromRequest.get;
    // mapViewer情報
    val mapViewer = mapViewerDAO.selectAll()
    // viewType情報
    val viewType = viewTypeDAO.selectAll()

    exbViewerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.manage.exbViewerManager(formWithErrors, exbViewer,mapViewer,viewType)),
      ExbViewerData => {
        exbViewerData = exbViewerForm.bindFromRequest.get
        System.out.println("exbViewerData" + exbViewerData);
        //System.out.println("exbViewerData.viewerId(0)" + exbViewerData.viewerId(0));

        val result = exDAO.updateExbMaster(new ExbMasterData(exbViewerData.viewerId, exbViewerData.viewerVisible, exbViewerData.viewerPosType, exbViewerData.viewerPosX, exbViewerData.viewerPosY, exbViewerData.viewerPosMargin, exbViewerData.viewerPosCount, exbViewerData.viewerPosFloor, exbViewerData.viewerPosSize, exbViewerData.viewerPosNum))
        System.out.println("exbViewerData.result////" + result);
        if (result == "success") {
          Redirect("/manage/exbViewerManager").flashing("resultOK" -> Messages("db.update.ok"))
        } else {
          Redirect("/manage/exbViewerManager").flashing("resultNG" -> Messages(result))
        }
      }
    )

  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if (super.isCmsLogged) {
      // exbViewer情報
      val placeId = super.getCurrentPlaceId
      val exbViewer = exDAO.selectExbAll(placeId)

      // mapViewer情報
      val mapViewer = mapViewerDAO.selectAll()

      // viewType情報
      val viewType = viewTypeDAO.selectAll()

      Ok(views.html.manage.exbViewerManager(exbViewerForm, exbViewer,mapViewer,viewType))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
