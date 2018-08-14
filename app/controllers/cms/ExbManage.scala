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
//case class FloorUpdateForm(inputFloorId: String,inputDisplayOrder:String,activeFlg:Boolean,inputFloorName: String)

@Singleton
class ExbManage @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , placeDAO: models.placeDAO
  , floorDAO: models.floorDAO
  , exbDAO: models.ExbDAO
  ) extends BaseController with I18nSupport {

//  /** フロア更新 */
//  def floorUpdate = SecuredAction { implicit request =>
//    // フォームの準備
//    val inputForm = Form(mapping(
//      "inputFloorId" -> text
//      ,"inputDisplayOrder" -> text.verifying(Messages("error.cms.exbManage.floorUpdate.displayOrder.empty"), {!_.isEmpty})
//      ,"activeFlg" -> boolean
//      , "inputFloorName" -> text.verifying(Messages("error.cms.exbManage.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
//      // , "inputExbDeviceNoListComma" -> text.verifying(Messages("error.cms.exbManage.floorUpdate.inputExbDeviceNoListComma.empty"), {!_.isEmpty})
//    )(FloorUpdateForm.apply)(FloorUpdateForm.unapply))
//    val placeId = securedRequest2User.currentPlaceId.get
//    // フォームの取得
//    val form = inputForm.bindFromRequest
//    if (form.hasErrors){
//      // エラーメッセージ
//      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
//      // リダイレクトで画面遷移
//      Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
//    }else{
//      val f = form.get
//      if(f.inputFloorId.isEmpty){// 新規フロア登録の場合
//        // DB処理
//        floorDAO.insert(f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
//        // 成功で遷移
//        Redirect(routes.ExbManage.index)
//          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ExbManage.floorUpdate"))
//
//      }else{  // フロア更新の場合 --------------------------
//        // DB処理
//        floorDAO.update(f.inputFloorId.toInt,f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
//        Redirect(routes.ExbManage.index)
//          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ExbManage.floorUpdate"))
//      }
//    }
//  }
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
      val vFloorId = form.get.floorId

      //vFloorId
      val placeId = securedRequest2User.currentPlaceId.get
      val exbInfoList = exbDAO.selectExbAll(placeId)
    //  val floorInfoList = floorDAO.selectFloorInfoData(placeId)
     // val vTarget = floorInfoList.filter(_.floor_id == vDeleteExbId)
      val vTarget2 = exbInfoList.filter(_.exb_id == vDeleteExbId)
      //val vAlreadySetupExb = vTarget.last.exbDeviceIdList.last


      Redirect(routes.ExbManage.index)
                .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ExbManage.exbDelete"))

      //Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.exbManage.delete.exb"))
//      if(vAlreadySetupExb.nonEmpty){  // exbが設置されてる
//        Redirect(routes.ExbManage.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.exbManage.delete.exb"))
//      }else{  // 正常の場合削除を行う
//        floorDAO.deleteById(vDeleteFloorId) // 削除処理
//        Redirect(routes.ExbManage.index)
//          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorDelete"))
//      }
    }
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3){
      // 選択されている現場の現場ID
      val placeId = securedRequest2User.currentPlaceId.get
      // フロア情報の取得
      //val floorInfoList = floorDAO.selectFloorInfoData(placeId)
      val exbInfoList = exbDAO.selectExbAll(placeId)

      Ok(views.html.cms.exbManage(exbInfoList))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }

  }

}
