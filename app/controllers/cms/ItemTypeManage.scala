package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.site
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 仮設材種別管理アクションクラス
  *
  *
  */

// フォームの定義
case class ItemTypeUpdateForm(inputPlaceId: String,
                              inputItemTypeId: String,
                              inputItemTypeName: String,
                              inputItemTypeCategory: String,
                              inputItemTypeIconColor: String,
                              inputItemTypeTextColor: String,
                              inputItemTypeRowColor: String,
                              inputNote: String)
case class ItemTypeDeleteForm(deleteItemTypeId: String)

@Singleton
class ItemTypeManage @Inject()(config: Configuration
                              , val silhouette: Silhouette[MyEnv]
                              , val messagesApi: MessagesApi
                              , itemTypeDAO: models.ItemTypeDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 仮設材種別情報
      val itemTypeList = itemTypeDAO.selectItemTypeInfo(placeId)
      Ok(views.html.cms.itemTypeManage(itemTypeList, placeId))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      , "inputItemTypeId" -> text
      , "inputItemTypeName" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeName.empty"), {!_.isEmpty})
      , "inputItemTypeCategory" -> text
      , "inputItemTypeIconColor" -> text
      , "inputItemTypeTextColor" -> text
      , "inputItemTypeRowColor" -> text
      , "inputNote" -> text
    )(ItemTypeUpdateForm.apply)(ItemTypeUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)

      // リダイレクトで画面遷移
      Redirect(routes.ItemTypeManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      var errMsg = Seq[String]()
      val f = form.get

      if(checkRGB(f.inputItemTypeIconColor)){
        if(checkRGB(f.inputItemTypeTextColor)){
          if(checkRGB(f.inputItemTypeRowColor)){
            if(f.inputItemTypeId.isEmpty){
              // 新規登録の場合 --------------------------
              // DB処理
              itemTypeDAO.insert(f.inputItemTypeName, f.inputItemTypeCategory, f.inputItemTypeIconColor, f.inputItemTypeTextColor, f.inputItemTypeRowColor, f.inputNote, f.inputPlaceId.toInt)

              Redirect(routes.ItemTypeManage.index)
                .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.update"))

            }else{
              // 更新の場合 --------------------------
              // DB処理
              itemTypeDAO.updateById(f.inputItemTypeId.toInt, f.inputItemTypeName, f.inputItemTypeCategory, f.inputItemTypeIconColor, f.inputItemTypeTextColor, f.inputItemTypeRowColor, f.inputNote)

              Redirect(routes.ItemTypeManage.index)
                .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.update"))
            }
          }else{
            Redirect(routes.ItemTypeManage.index)
              .flashing(SUCCESS_MSG_KEY -> Messages("error.cms.ItemTypeManage.RowColor"))
          }
        }else {
          Redirect(routes.ItemTypeManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("error.cms.ItemTypeManage.TextColor"))
        }

      }else{
          Redirect(routes.ItemTypeManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("error.cms.ItemTypeManage.IconColor"))
      }
    }
  }

  /** RGBチェック「rgb(255,255,255)形式」 */
  def checkRGB(Inpar: String): Boolean = {
    // RGB 範囲チェック
    var resFlg: Boolean = true
    var rgbWk = Inpar
    var rgb = rgbWk.substring(4, rgbWk.length())
    rgb = rgb.replace(")", "")
    val listRgb = rgb.split(",")
    if(listRgb.length == 3) {
      for (rgbInt <- listRgb) {
        if (BigDecimal(rgbInt) < 0 || BigDecimal(rgbInt) > 255) {
          resFlg = false
        }
      }
    }else{
      resFlg = false
    }
    return resFlg
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "deleteItemTypeId" -> text.verifying(Messages("error.cms.ItemTypeManage.delete.empty"), {!_.isEmpty})
    )(ItemTypeDeleteForm.apply)(ItemTypeDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemTypeManage.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get
      // DB処理
      itemTypeDAO.deleteById(f.deleteItemTypeId.toInt)

      // リダイレクト
      Redirect(routes.ItemTypeManage.index).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.delete"))
    }
  }

}
