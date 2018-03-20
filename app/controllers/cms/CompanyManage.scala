package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 業者管理アクションクラス
  *
  *
  */
case class CompanyUpdateForm(inputPlaceId: String, inputCompanyId: String, inputCompanyName: String, inputNote: String = "")
case class CompanyDeleteForm(inputCompanyId: String)

@Singleton
class CompanyManage @Inject()(config: Configuration
                              , val silhouette: Silhouette[MyEnv]
                              , val messagesApi: MessagesApi
                              , companyDAO: models.companyDAO
                               ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 選択された現場の現場ID
    val placeIdStr = super.getCurrentPlaceIdStr

    // 業者情報
    val companyList = companyDAO.selectCompany(placeIdStr.toInt)

    Ok(views.html.cms.companyManage(companyList, placeIdStr.toInt))
  }


  /** フロア更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      , "inputCompanyId" -> text
      , "inputCompanyName" -> text.verifying(Messages("error.cms.CompanyManage.update.inputCompanyName.empty"), {!_.isEmpty})
      , "inputNote" -> text
    )(CompanyUpdateForm.apply)(CompanyUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.CompanyManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      var errMsg = Seq[String]()
      val f = form.get
      if(f.inputCompanyId.isEmpty){
        // 新規登録の場合 --------------------------
        // 名称重複チェック
        val companyList = companyDAO.selectCompany(super.getCurrentPlaceIdStr.toInt, f.inputCompanyName)
        if(companyList.length > 0){
          errMsg :+= Messages("error.cms.CompanyManage.update.inputCompanyName.duplicate")
        }
        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.CompanyManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          companyDAO.insert(f.inputCompanyName, f.inputNote, f.inputPlaceId.toInt)

          Redirect(routes.CompanyManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CompanyManage.update"))
        }

      }else{
        // 更新の場合 --------------------------
        // 名称重複チェック
        var companyList = companyDAO.selectCompany(super.getCurrentPlaceIdStr.toInt)
        companyList = companyList.filter(_.companyId != f.inputCompanyId.toInt).filter(_.companyName == f.inputCompanyName)
        if(companyList.length > 0){
          errMsg :+= Messages("error.cms.CompanyManage.update.inputCompanyName.duplicate")
        }
        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.CompanyManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          companyDAO.updateById(f.inputCompanyId.toInt, f.inputCompanyName, f.inputNote)

          Redirect(routes.CompanyManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CompanyManage.update"))
        }
      }
    }
  }


  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputCompanyId" -> text
    )(CompanyDeleteForm.apply)(CompanyDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    val f = form.get
    // DB処理
    companyDAO.deleteById(f.inputCompanyId.toInt)

    // リダイレクト
    Redirect(routes.CompanyManage.index)
      .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CompanyManage.delete"))
  }

}
