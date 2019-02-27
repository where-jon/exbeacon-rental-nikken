package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import models.manage.ExbMasterData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * EXB設置管理クラス.
  */
class ExbSetupController @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , ws: WSClient
  , exbDAO: models.manage.ExbDAO
  , floorDAO: models.manage.floorDAO
  , viewTypeDAO: models.ViewTypeDAO
  ) extends BaseController with I18nSupport {

   /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3){
      // exbMaster情報
      val placeId = super.getCurrentPlaceId
      val exbViewer = exbDAO.selectExbAll(placeId)

      // map情報
      val mapViewer = floorDAO.selectFloorAll(placeId)

      // viewType情報
      val viewType = viewTypeDAO.selectAll()

      Ok(views.html.manage.exbSetup(exbViewerForm, exbViewer,mapViewer,viewType))
    }else{
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

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


  def updateExbSetup = SecuredAction { implicit request =>
    // 部署情報
    val placeId = super.getCurrentPlaceId
    val exbViewer = exbDAO.selectExbAll(placeId)
    var exbViewerData = exbViewerForm.bindFromRequest.get;
    // mapViewer情報
    val mapViewer = floorDAO.selectFloorAll(placeId)
    // viewType情報
    val viewType = viewTypeDAO.selectAll()

    exbViewerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.manage.exbSetup(formWithErrors, exbViewer,mapViewer,viewType)),
      ExbViewerData => {
        exbViewerData = exbViewerForm.bindFromRequest.get
        val result = exbDAO.updateExbMaster(new ExbMasterData(exbViewerData.viewerId, exbViewerData.viewerVisible, exbViewerData.viewerPosType, exbViewerData.viewerPosX, exbViewerData.viewerPosY, exbViewerData.viewerPosMargin, exbViewerData.viewerPosCount, exbViewerData.viewerPosFloor, exbViewerData.viewerPosSize, exbViewerData.viewerPosNum))
        if (result == "success") {
          Redirect("/manage/exbSetup").flashing("resultOK" -> Messages("db.update.ok"))
        } else {
          Redirect("/manage/exbSetup").flashing("resultNG" -> Messages(result))
        }
      }
    )

  }


}
