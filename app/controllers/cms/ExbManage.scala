package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * フロア管理アクションクラス
  *
  *
  */

// フォーム定義
case class ExbDeleteForm(deleteExbId: String, floorId: String)
case class ExbUpdateForm(
  inputExbId: String
  ,inputDeviceId:String
  ,inputDeviceNo:String
  ,inputDeviceName: String
  ,inputPosName: String
  ,setupFloorId: String
)

@Singleton
class ExbManage @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , placeDAO: models.placeDAO
  , floorDAO: models.floorDAO
  , exbDAO: models.ExbDAO
  ) extends BaseController with I18nSupport {

  /** フロア更新 */
  def exbUpdate = SecuredAction { implicit request =>
    val inputForm = Form(mapping(
      "inputExbId" -> text
      , "inputDeviceId" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.inputDeviceId.empty"), {_.matches("^[0-9]+$")})
      , "inputDeviceNo" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.inputDeviceNo.empty"), {_.matches("^[0-9]+$")})
      , "inputDeviceName" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.inputDeviceName.empty"), {!_.isEmpty})
      , "inputPosName" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.inputPosName.empty"), {!_.isEmpty})
      , "setupFloorId" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.setupFloorId.empty"), {_.matches("^[-1-9]+$")})
    )(ExbUpdateForm.apply)(ExbUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val placeId = securedRequest2User.currentPlaceId.get
      val f = form.get
      if(f.inputExbId.isEmpty) {  // 新規EXB登録の場合
        // DB処理
        exbDAO.insertData(f.inputDeviceId.toInt,f.inputDeviceNo.toInt,f.inputDeviceName,f.inputPosName,f.setupFloorId.toInt,placeId)
        Redirect(routes.ExbManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.exbManage.exbUpdate"))
      }else{ // EXB更新の場合
        exbDAO.update(f.inputDeviceId.toInt,f.inputDeviceNo.toInt,f.inputDeviceName,f.inputPosName,f.setupFloorId.toInt,f.inputExbId.toInt,placeId)
        Redirect(routes.ExbManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.exbManage.exbUpdate"))
      }

    }

  }
  /** フロア削除 */
  def exbDelete = SecuredAction { implicit request =>
    // フォームの準備
    val deleteForm = Form(mapping(
      "deleteExbId" -> text.verifying(Messages("error.cms.exbManage.exbUpdate.delete.empty"), {!_.isEmpty}),
      "floorId"  -> text
    )(ExbDeleteForm.apply)(ExbDeleteForm.unapply))

    // フォームの取得
    val form = deleteForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      val vDeleteExbId = form.get.deleteExbId.toInt
      val vFloorId = form.get.floorId.toInt
      val placeId = securedRequest2User.currentPlaceId.get
      val floorInfoList = floorDAO.selectFloorInfoData(placeId)
      val vTarget = floorInfoList.filter(_.floor_id == vFloorId)

      val vAlreadySetupExb =
      if(vTarget.isEmpty) ""
      else vTarget.last.exbDeviceIdList.last

      if(vAlreadySetupExb.nonEmpty) { // exbが設置されてる
        Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.floorManage.delete.exb"))
      }else{  // 正常の場合削除を行う
        exbDAO.deleteById(vDeleteExbId) // 削除処理
        Redirect(routes.ExbManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ExbManage.exbDelete"))
      }
    }
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3){
      // 選択されている現場の現場ID
      val placeId = securedRequest2User.currentPlaceId.get
      // フロア情報の取得
      val floorInfoList = floorDAO.selectFloorInfoData(placeId)
      // exb情報の取得
      val exbInfoList = exbDAO.selectExbAll(placeId)

      Ok(views.html.cms.exbManage(exbInfoList,floorInfoList))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }

  }

}
