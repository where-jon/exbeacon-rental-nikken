package controllers.manage

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import controllers.BaseController
import controllers.site
import models.manage._
import models.User
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.{MyEnv, UserService}

/**
  * 現場アカウント管理のコントローラー
  */
@Singleton
class AccountController @Inject()(
  config: Configuration,
  val silhouette: Silhouette[MyEnv],
  val messagesApi: MessagesApi,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService
) extends BaseController with I18nSupport {

  /**
    * 初期表示
    */
  def index = SecuredAction { implicit request =>
    if (request.identity.level >= 2){
      Ok(views.html.manage.account(
        userService.selectAccountByPlaceId(placeId = super.getCurrentPlaceId),
        UserLevelEnum().map)
      )
    }else {
      // 権限無いとき退場
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

  /**
    * アカウント新規作成
    */
  def create = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userName" -> text.verifying(Messages("error.manage.Account.create.userName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.manage.Account.create.userLoginId.empty"), {!_.isEmpty}),
        "userLevel" -> text.verifying(Messages("error.manage.Account.create.userLevel.empty"), {!_.isEmpty}),
        "userPassword1" -> text.verifying(Messages("error.manage.Account.create.userPassword1.empty"), {!_.isEmpty}),
        "userPassword2" -> text.verifying(Messages("error.manage.Account.create.userPassword2.empty"), {!_.isEmpty})
      )
      (AccountCreateForm.apply)
      (AccountCreateForm.unapply)
      verifying (
        Messages("error.manage.Account.create.passwords.notEqual"),
        form => form.userPassword1 == form.userPassword2
      )
      verifying (
        Messages("error.manage.Account.create.userLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.selectByLoginId(form.userLoginId).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.AccountController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val placeId = super.getCurrentPlaceId
      val hs = passwordHasherRegistry.current.hash(form.get.userPassword1)
      val user = User.apply(
        Option(0), form.get.userLoginId, true, hs.password, form.get.userName,
        Option(placeId), Option(placeId), "",
        true, form.get.userLevel.toInt, null
      )
      userService.insert(user)
      Redirect(routes.AccountController.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.Account.create"))
    }
  }

  /**
    * アカウント更新
    */
  def update = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userId" -> text.verifying(Messages("error.manage.Account.update.userId.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.manage.Account.update.userName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.manage.Account.update.userLoginId.empty"), {!_.isEmpty}),
        "userLevel" -> text.verifying(Messages("error.manage.Account.update.userLevel.empty"), {!_.isEmpty})
      )
      (AccountUpdateForm.apply)
      (AccountUpdateForm.unapply)
      verifying (
        Messages("error.manage.Account.update.userLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.checkExistByLoginId(form.userLoginId, form.userId.toInt).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.AccountController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val inForm = form.get
      userService.updateUserNameLevelById(inForm.userId, inForm.userName, inForm.userLoginId, inForm.userLevel)
      Redirect(routes.AccountController.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.Account.update"))
    }
  }

  /** パスワード更新 */
  def passwordUpdate = SecuredAction { implicit request =>
    val inputForm = Form(mapping(
      "userId" -> text.verifying(Messages("error.manage.Account.passwordUpdate.userId.empty"), {!_.isEmpty}),
      "userPassword1" -> text.verifying(Messages("error.manage.Account.passwordUpdate.userPassword1.empty"), {!_.isEmpty}),
      "userPassword2" -> text.verifying(Messages("error.manage.Account.passwordUpdate.userPassword2.empty"), {!_.isEmpty})
    )
    (AccountPasswordUpdateForm.apply)(AccountPasswordUpdateForm.unapply)
    verifying (
      Messages("error.manage.Account.passwordUpdate.notEqual"),
      form => form.userPassword1 == form.userPassword2
    ))
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.AccountController.index())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
        userService.changePasswordById(
          form.get.userId,
          passwordHasherRegistry.current.hash(form.get.userPassword1).password
        )
        Redirect(routes.AccountController.index.path)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.Account.passwordUpdate"))
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userId" -> text.verifying(Messages("error.manage.Account.delete.userId.empty"), {!_.isEmpty})
      )
      (AccountDeleteForm.apply)(AccountDeleteForm.unapply)
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.AccountController.index())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
      userService.deleteLogicalById(form.get.userId)
      Redirect(routes.AccountController.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.Account.delete"))
    }
  }

}
