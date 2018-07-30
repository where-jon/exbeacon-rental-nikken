package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import models.ExbData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * EXB管理クラス.
  */
class ExbManager @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , ws: WSClient
                           , exbDAO: models.ExbDAO
                          ) extends BaseController with I18nSupport {

  val exbForm = Form(mapping(
    "exbId" -> list(number),
    "exbEditId" -> list(number),
    "exbDeviceId" -> list(nonEmptyText),
    "exbName" -> list(text),
    "exbPosName" -> list(text)

  )(ExbData.apply)(ExbData.unapply))

  def deleteExb = SecuredAction { implicit request =>

    // exbMaster情報
    val placeId = super.getCurrentPlaceId
    val exb = exbDAO.selectExbAll(placeId)

    exbForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.manage.exbManager(formWithErrors, exb)),
      ExbData => {
        val exbData = exbForm.bindFromRequest.get
        System.out.println("exbData" + exbData);
        System.out.println("exbData.exbId(0)" + exbData.exbId(0));

        val result = exbDAO.deleteExb(new ExbData(exbData.exbId, exbData.exbEditId, exbData.exbDeviceId, exbData.exbName, exbData.exbPosName))
        System.out.println("exbData.result////" + result);
        if (result == "success") {
          Redirect("/manage/exbManager").flashing("resultOK" -> Messages("db.delete.ok"))
        } else {
          Redirect("/manage/exbManager").flashing("resultNG" -> Messages(result))
        }
      }

    )
  }

  /*20180730 EXB管理アップデート関連処理はまだ未着手*/
  def updateExb = SecuredAction { implicit request =>
    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------start updateExb:");
    // exbMaster情報
    val placeId = super.getCurrentPlaceId
    val exb = exbDAO.selectExbAll(placeId)
    var exbData = exbForm.bindFromRequest.get;
    exbForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.manage.exbManager(formWithErrors, exb)),
      ExbData => {
        exbData = exbForm.bindFromRequest.get
        val result = exbDAO.updateExb(new ExbData(exbData.exbId, exbData.exbEditId, exbData.exbDeviceId, exbData.exbName, exbData.exbPosName))
        System.out.println("exbData.result////" + result);
        if (result == "success") {
          Redirect("/manage/exbManager").flashing("resultOK" -> Messages("db.update.ok"))
        } else {
          Redirect("/manage/exbManager").flashing("resultNG" -> Messages(result))
        }
      }
    )

  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3){
      // exbMaster情報
      val placeId = super.getCurrentPlaceId
      val vExb = exbDAO.selectExbAll(placeId)

      //val vExb = exbDAO.selectAll()
      Ok(views.html.manage.exbManager(exbForm, vExb))
    }else{
      Redirect(site.routes.WorkPlace.index)
    }
  }

}
