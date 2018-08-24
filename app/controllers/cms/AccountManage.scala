package controllers.cms

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import controllers.BaseController
import controllers.site
import models.{UserLevelEnum, User}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.{MyEnv, UserService}

/**
  * アカウント新規作成時のForm
  */
case class AccountCreateForm(
  userName: String, // アカウント名
  userLoginId: String, // アカウントID
  userLevel: String, // アカウント権限
  userPassword1: String, // パスワード
  userPassword2: String // 確認用パスワード
)

/**
  * アカウント更新時のForm
  */
case class AccountUpdateForm(
  userId: String, // アカウントID
  userName: String, // アカウント名
  userLoginId: String, // ログインID
  userLevel: String // アカウント権限
)

/**
  * アカウントパスワード更新時のForm
  */
case class AccountPasswordUpdateForm(
  userId: String,
  userPassword1: String,
  userPassword2: String
)

/**
  * アカウント削除時のForm
  */
case class AccountDeleteForm(
  userId: String
)

/**
  * 現場アカウント管理のコントローラー
  */
@Singleton
class AccountManage @Inject()(
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
      Ok(views.html.cms.accountManage(
        userService.selectAccountByPlaceId(placeId = super.getCurrentPlaceId),
        UserLevelEnum().map)
      )
    }else {
      // 権限無いとき退場
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /**
    * アカウント新規作成
    */
  def create = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userName" -> text.verifying(Messages("error.cms.AccountManage.create.userName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.cms.AccountManage.create.userLoginId.empty"), {!_.isEmpty}),
        "userLevel" -> text.verifying(Messages("error.cms.AccountManage.create.userLevel.empty"), {!_.isEmpty}),
        "userPassword1" -> text.verifying(Messages("error.cms.AccountManage.create.userPassword1.empty"), {!_.isEmpty}),
        "userPassword2" -> text.verifying(Messages("error.cms.AccountManage.create.userPassword2.empty"), {!_.isEmpty})
      )
      (AccountCreateForm.apply)
      (AccountCreateForm.unapply)
      verifying (
        Messages("error.cms.AccountManage.create.passwords.notEqual"),
        form => form.userPassword1 == form.userPassword2
      )
      verifying (
        Messages("error.cms.AccountManage.create.userLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.selectByLoginId(form.userLoginId).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.AccountManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val placeId = super.getCurrentPlaceId
      val hs = passwordHasherRegistry.current.hash(form.get.userPassword1)
      val user = User.apply(
        Option(0), form.get.userLoginId, true, hs.password, form.get.userName,
        Option(placeId), Option(placeId), "",
        true, form.get.userLevel.toInt, null
      )
      userService.insert(user)
      Redirect(routes.AccountManage.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.AccountManage.create"))
    }
  }

  /**
    * アカウント更新
    */
  def update = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userId" -> text.verifying(Messages("error.cms.AccountManage.update.userId.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.cms.AccountManage.update.userName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.cms.AccountManage.update.userLoginId.empty"), {!_.isEmpty}),
        "userLevel" -> text.verifying(Messages("error.cms.AccountManage.update.userLevel.empty"), {!_.isEmpty})
      )
      (AccountUpdateForm.apply)
      (AccountUpdateForm.unapply)
      verifying (
        Messages("error.cms.AccountManage.update.userLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.checkExistByLoginId(form.userLoginId, form.userId.toInt).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.AccountManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val inForm = form.get
      userService.updateUserNameLevelById(inForm.userId, inForm.userName, inForm.userLoginId, inForm.userLevel)
      Redirect(routes.AccountManage.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.AccountManage.update"))
    }
  }

  /** パスワード更新 */
  def passwordUpdate = SecuredAction { implicit request =>
    val inputForm = Form(mapping(
      "userId" -> text.verifying(Messages("error.cms.AccountManage.passwordUpdate.userId.empty"), {!_.isEmpty}),
      "userPassword1" -> text.verifying(Messages("error.cms.AccountManage.passwordUpdate.userPassword1.empty"), {!_.isEmpty}),
      "userPassword2" -> text.verifying(Messages("error.cms.AccountManage.passwordUpdate.userPassword2.empty"), {!_.isEmpty})
    )
    (AccountPasswordUpdateForm.apply)(AccountPasswordUpdateForm.unapply)
    verifying (
      Messages("error.cms.AccountManage.passwordUpdate.notEqual"),
      form => form.userPassword1 == form.userPassword2
    ))
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.AccountManage.index())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
        userService.changePasswordById(
          form.get.userId,
          passwordHasherRegistry.current.hash(form.get.userPassword1).password
        )
        Redirect(routes.AccountManage.index.path)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.AccountManage.passwordUpdate"))
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    val inputForm = Form(
      mapping(
        "userId" -> text.verifying(Messages("error.cms.AccountManage.delete.userId.empty"), {!_.isEmpty})
      )
      (AccountDeleteForm.apply)(AccountDeleteForm.unapply)
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.AccountManage.index())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
      userService.deleteLogicalById(form.get.userId)
      Redirect(routes.AccountManage.index.path)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.AccountManage.delete"))
    }
  }

}
