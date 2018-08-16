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
case class FloorDeleteForm(deleteFloorId: String)
case class FloorUpdateForm(inputFloorId: String,inputDisplayOrder:String,activeFlg:Boolean,inputFloorName: String)

@Singleton
class FloorManage @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , placeDAO: models.placeDAO
  , floorDAO: models.floorDAO
  , exbDAO: models.exbModelDAO
  ) extends BaseController with I18nSupport {

  /** フロア更新 */
  def floorUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputFloorId" -> text
      ,"inputDisplayOrder" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.displayOrder.empty"), {_.matches("^[-1-9]+$")})
      ,"activeFlg" -> boolean
      , "inputFloorName" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
    )(FloorUpdateForm.apply)(FloorUpdateForm.unapply))
    val placeId = securedRequest2User.currentPlaceId.get
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get
      if(f.inputFloorId.isEmpty){// 新規フロア登録の場合
        // DB処理
        floorDAO.insert(f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
        // 成功で遷移
        Redirect(routes.FloorManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorUpdate"))

      }else{  // フロア更新の場合 --------------------------
        // DB処理
        floorDAO.update(f.inputFloorId.toInt,f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
        Redirect(routes.FloorManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorUpdate"))
      }
    }
  }
   /** フロア削除 */
  def floorDelete = SecuredAction { implicit request =>
    // フォームの準備
    val deleteForm = Form(mapping(
        "deleteFloorId" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.delete.empty"), {!_.isEmpty})
    )(FloorDeleteForm.apply)(FloorDeleteForm.unapply))

    // フォームの取得
    val form = deleteForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      val vDeleteFloorId = form.get.deleteFloorId.toInt
      val placeId = securedRequest2User.currentPlaceId.get
      val floorInfoList = floorDAO.selectFloorInfoData(placeId)
      val vTarget = floorInfoList.filter(_.floor_id == vDeleteFloorId)
      val vAlreadySetupExb = vTarget.last.exbDeviceIdList.last
      if(vAlreadySetupExb.nonEmpty){  // exbが設置されてる
        Redirect(routes.FloorManage.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.floorManage.delete.exb"))
      }else{  // 正常の場合削除を行う
       floorDAO.deleteById(vDeleteFloorId) // 削除処理
        Redirect(routes.FloorManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorDelete"))
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
      Ok(views.html.cms.floorManage(floorInfoList))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }

  }

}
