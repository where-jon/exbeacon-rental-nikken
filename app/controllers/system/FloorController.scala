package controllers.system

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
case class FloorUpdateForm(inputFloorId: String,inputPreDisplayOrder:String,inputDisplayOrder:String,activeFlg:Boolean,inputFloorName: String)

@Singleton
class FloorController @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , placeDAO: models.placeDAO
  , floorDAO: models.system.floorDAO
  , exbDAO: models.system.ExbDAO
  ) extends BaseController with I18nSupport {

  /** フロア更新 */
  def floorUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputFloorId" -> text
      ,"inputPreDisplayOrder" -> text
      ,"inputDisplayOrder" -> text.verifying(Messages("error.system.floor.floorUpdate.displayOrder.empty"), {_.matches("^[0-9]+$")})
      ,"activeFlg" -> boolean
      , "inputFloorName" -> text.verifying(Messages("error.system.floor.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
    )(FloorUpdateForm.apply)(FloorUpdateForm.unapply))
    val placeId = securedRequest2User.currentPlaceId.get
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get
      val vDisplayOrderDuplicateCheck =
      if(f.inputFloorId.isEmpty) {   // 新規の場合チェック必ずチェック
        floorDAO.floorDisplayOrderCheck(f.inputDisplayOrder.toInt,placeId).length
      }else if (f.inputPreDisplayOrder!= f.inputDisplayOrder){
        floorDAO.floorDisplayOrderCheck(f.inputDisplayOrder.toInt,placeId).length
      }else {
        0
      }
      // 編集の場合前回表示順と現在表示順が違う場合チェックを行う
      if(vDisplayOrderDuplicateCheck > 0){   //display_order重複判断
        Redirect(routes.FloorController.index()).flashing(ERROR_MSG_KEY -> Messages("error.system.floor.floorUpdate.inputDisplayOrder.duplicate",f.inputDisplayOrder.toInt))
      }else {
        if(f.inputFloorId.isEmpty){// 新規フロア登録の場合
          // DB処理
          floorDAO.insert(f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
          // 成功で遷移
          Redirect(routes.FloorController.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.system.floor.floorUpdate"))

        }else{  // フロア更新の場合 --------------------------
          // DB処理
          floorDAO.update(f.inputFloorId.toInt,f.inputFloorName,f.inputDisplayOrder.toInt,f.activeFlg,placeId)
          Redirect(routes.FloorController.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.system.floor.floorUpdate"))
        }
      }
    }
  }
   /** フロア削除 */
  def floorDelete = SecuredAction { implicit request =>
    // フォームの準備
    val deleteForm = Form(mapping(
        "deleteFloorId" -> text.verifying(Messages("error.system.floor.floorUpdate.delete.empty"), {!_.isEmpty})
    )(FloorDeleteForm.apply)(FloorDeleteForm.unapply))

    // フォームの取得
    val form = deleteForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      val vDeleteFloorId = form.get.deleteFloorId.toInt
      val placeId = securedRequest2User.currentPlaceId.get
      val floorInfoList = floorDAO.selectFloorInfoData(placeId)
      val vTarget = floorInfoList.filter(_.floor_id == vDeleteFloorId)
      val vAlreadySetupExb = vTarget.last.exbDeviceIdList.last
      if(vAlreadySetupExb.nonEmpty){  // exbが設置されてる
        Redirect(routes.FloorController.index()).flashing(ERROR_MSG_KEY -> Messages("error.system.floor.delete.exb"))
      }else{  // 正常の場合削除を行う
       floorDAO.deleteById(vDeleteFloorId) // 削除処理
        Redirect(routes.FloorController.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.system.floor.floorDelete"))
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
      Ok(views.html.system.floor(floorInfoList))
    }else {
      Redirect(site.routes.ItemCarListController.index)
    }

  }

}
