package controllers

import models._
import utils.silhouette._
import utils.silhouette.Implicits._
import com.mohiva.play.silhouette.api.{ Silhouette, LoginInfo, SignUpEvent, LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Credentials, PasswordHasherRegistry, Clock }
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.{ IdentityNotFoundException, InvalidPasswordException }
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{ MessagesApi, Messages }
import utils.MailService
import utils.Mailer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import net.ceedubs.ficus.Ficus._
import javax.inject.{ Inject, Singleton }
import views.html.{ auth => viewsAuth }

case class ChangePassword2Form(
  selectUser: String = "",
  devUser: String = "",
  devUserCurrent: String = "",
  devUserpassword1: String = "",
  devUserpassword2: String = "",
  sysUser: String = "",
  sysUserCurrent: String = "",
  sysUserpassword1: String = "",
  sysUserpassword2: String = ""
)


@Singleton
class Auth @Inject() (
    val silhouette: Silhouette[MyEnv],
    val messagesApi: MessagesApi,
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    tokenService: MailTokenService[MailTokenUser],
    passwordHasherRegistry: PasswordHasherRegistry,
    mailService: MailService,
    conf: Configuration,
    clock: Clock
) extends AuthController {

  // UTILITIES

  implicit val ms = mailService
  val passwordValidation = nonEmptyText(minLength = 6)
  def notFoundDefault(implicit request: RequestHeader) = Future.successful(NotFound(views.html.errors.notFound(request)))

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SIGN IN

  val signInForm = Form(tuple(
    "identifier" -> text,
    "password" -> text,
    "rememberMe" -> boolean
  ))

  /**
   * Starts the sign in mechanism. It shows the login form.
   */
  def signIn = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(viewsAuth.signIn(signInForm))
    }
  }

  /**
   * Authenticates the user based on his email and password
   */
  def authenticate = UnsecuredAction.async { implicit request =>
    signInForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsAuth.signIn(formWithErrors))),
      formData => {
        val (identifier, password, rememberMe) = formData
        credentialsProvider.authenticate(Credentials(identifier, password)).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            // 認証成功
            case Some(user) => for {
              authenticator <- env.authenticatorService.create(loginInfo).map(authenticatorWithRememberMe(_, true))
              cookie <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(cookie, Redirect(routes.Application.index))
            } yield {
              env.eventBus.publish(LoginEvent(user, request))
              result
            }
            case None =>
              Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case ie: IdentityNotFoundException
            =>
              // アカウントなしエラー
              Logger.debug("アカウントなし：" + identifier + " / " + password + " / " + ie.getMessage )

              Redirect(routes.Auth.signIn).flashing("error" -> Messages("auth.credentials.incorrect"))
          case pe: InvalidPasswordException
            =>
              // パスワード違いエラー
              Logger.debug("パスワード違い：" + identifier + " / " + password + " / " + pe.getMessage)

              Redirect(routes.Auth.signIn).flashing("error" -> Messages("auth.credentials.incorrect"))
          case e: ProviderException
            =>
              // その他のエラー
              Logger.debug(e.getMessage + identifier + " / " + password )
              Redirect(routes.Auth.signIn).flashing("error" -> Messages("auth.credentials.incorrect"))
        }
      }
    )
  }

  private def authenticatorWithRememberMe(authenticator: CookieAuthenticator, rememberMe: Boolean) = {
    if (rememberMe) {
      authenticator.copy(
        expirationDateTime = clock.now + rememberMeParams._1,
        idleTimeout = rememberMeParams._2,
        cookieMaxAge = rememberMeParams._3
      )
    } else
      authenticator
  }
  private lazy val rememberMeParams: (FiniteDuration, Option[FiniteDuration], Option[FiniteDuration]) = {
    val cfg = conf.getConfig("silhouette.authenticator.rememberMe").get.underlying
    (
      cfg.as[FiniteDuration]("authenticatorExpiry"),
      cfg.getAs[FiniteDuration]("authenticatorIdleTimeout"),
      cfg.getAs[FiniteDuration]("cookieMaxAge")
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SIGN OUT

  /**
   * Signs out the user
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request))
    env.authenticatorService.discard(request.authenticator, Redirect(routes.Application.index).withNewSession)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FORGOT PASSWORD

  val emailForm = Form(single("email" -> email))

  /**
   * Starts the reset password mechanism if the user has forgot his password. It shows a form to insert his email address.
   */
  def forgotPassword = UserAwareAction { implicit request =>
    request.identity match {
      case Some(_) => Redirect(routes.Application.index)
      case None => Ok(viewsAuth.forgotPassword(emailForm))
    }
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def handleForgotPassword = UnsecuredAction.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsAuth.forgotPassword(formWithErrors))),
      email => userService.retrieve(email).flatMap {
        case Some(_) => {
          val token = MailTokenUser(email, isSignUp = false)
          tokenService.create(token).map { _ =>
            Mailer.forgotPassword(email, link = routes.Auth.resetPassword(token.id).absoluteURL())
            Ok(viewsAuth.forgotPasswordSent(email))
          }
        }
        case None => Future.successful(BadRequest(viewsAuth.forgotPassword(emailForm.withError("email", Messages("auth.user.notexists")))))
      }
    )
  }

  val resetPasswordForm = Form(tuple(
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText
  ) verifying (Messages("auth.passwords.notequal"), passwords => passwords._2 == passwords._1))

  /**
   * Confirms the user's link based on the token and shows him a form to reset the password
   */
  def resetPassword(tokenId: String) = UnsecuredAction.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        Future.successful(Ok(viewsAuth.resetPassword(tokenId, resetPasswordForm)))
      }
      case Some(token) => {
        tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => notFoundDefault
    }
  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleResetPassword(tokenId: String) = UnsecuredAction.async { implicit request =>
    resetPasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsAuth.resetPassword(tokenId, formWithErrors))),
      passwords => {
        tokenService.retrieve(tokenId).flatMap {
          case Some(token) if (!token.isSignUp && !token.isExpired) => {
            val loginInfo: LoginInfo = token.email
            userService.retrieve(loginInfo).flatMap {
              case Some(user) => {
                for {
                  _ <- authInfoRepository.update(loginInfo, passwordHasherRegistry.current.hash(passwords._1))
                  authenticator <- env.authenticatorService.create(user.email)
                  result <- env.authenticatorService.renew(authenticator, Ok(viewsAuth.resetedPassword(user)))
                } yield {
                  tokenService.consume(tokenId)
                  env.eventBus.publish(LoginEvent(user, request))
                  result
                }
              }
              case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          }
          case Some(token) => {
            tokenService.consume(tokenId)
            notFoundDefault
          }
          case None => notFoundDefault
        }
      }
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CHANGE PASSWORD

  val changePasswordForm = Form(tuple(
    "current" -> nonEmptyText,
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText
  ) verifying (Messages("auth.passwords.notequal"), passwords => passwords._3 == passwords._2))

  var changePassword2Form = Form(
    mapping(
      "selectUser" -> nonEmptyText, // 開発管理者かシステム管理者か
      "devUser" -> text, // 開発管理者のID
      "devUserCurrent" -> text, // 開発管理者の現在のパスワード
      "devUserPassword1" -> text, // 開発管理者の変更したいパスワード
      "devUserPassword2" -> text, // 開発管理者の変更したいパスワードの確認用
      "sysUser" -> text, // システム管理者のID
      "sysUserCurrent" -> text, // システム管理者の現在のパスワード
      "sysUserPassword1" -> text, // システム管理者の変更したいパスワード
      "sysUserPassword2" -> text // システム管理者の変更したいパスワードの確認用
    )
    (ChangePassword2Form.apply)
    (ChangePassword2Form.unapply)
    verifying(
      Messages("error.changePassword.devUser.current.empty"), // "開発管理者の現在のパスワードは必須入力です。"
      form => {
        if (form.selectUser == "dev") {
          // 開発管理者を選択している場合は開発管理者の現在のパスワードは必須入力
          form.devUserCurrent != ""
        } else {
          true
        }
      }
    )
    verifying(
      Messages("error.changePassword.devUser.password1.empty"), // "開発管理者の新しいパスワードは必須入力です。",
      form => {
        if (form.selectUser == "dev") {
          // 開発管理者を選択している場合は開発管理者のパスワード1は必須入力
          form.devUserpassword1 != ""
        } else {
          true
        }
      }
    )
    verifying(
      Messages("error.changePassword.devUser.password2.notequal"), // "開発管理者の新しいパスワードと確認用パスワードが違います。",
      form => {
        if (form.selectUser == "dev") {
          // 開発管理者を選択している場合は開発管理者のパスワード1とパスワード2が同じであること
          form.devUserpassword1 == form.devUserpassword2
        } else {
          true
        }
      }
    )
    //
    verifying(
      Messages("error.changePassword.sysUser.current.empty"), // "システム管理者の現在のパスワードは必須入力です。",
      form => {
        if (form.selectUser == "sys") {
          // システム管理者を選択している場合はシステム管理者の現在のパスワードは必須入力
          form.sysUserCurrent != ""
        } else {
          true
        }
      }
    )
      verifying(
      Messages("error.changePassword.sysUser.password1.empty"), // "システム管理者の新しいパスワードは必須入力です。",
      form => {
        if (form.selectUser == "sys") {
          // システム管理者を選択している場合はシステム管理者のパスワード1は必須入力
          form.sysUserpassword1 != ""
        } else {
          true
        }
      }
    )
    verifying(
      Messages("error.changePassword.sysUser.password2.notequal"), // "システム管理者の新しいパスワードと確認用パスワードが違います。",
      form => {
        if (form.selectUser == "sys") {
          // システム管理者を選択している場合はシステム管理者のパスワード1とパスワード2が同じであること
          form.sysUserpassword1 == form.sysUserpassword2
        } else {
          true
        }
      }
    )
  )

  /**
   * Starts the change password mechanism. It shows a form to insert his current password and the new one.
   */
  def changePassword = SecuredAction { implicit request =>
    // Form初期値
    var passForm = changePassword2Form.fill(
      ChangePassword2Form.apply(
        selectUser = "dev", // 初期値は開発管理者
        devUser = userService.findDevUser().get.email, // 開発管理者のID
        sysUser = userService.findSysUser().get.email // システム管理者のID
      )
    )
    // Viewを表示
    Ok(viewsAuth.changePassword(passForm))
  }

  /**
   * Saves the new password and renew the cookie
   */
  def handleChangePassword = SecuredAction.async { implicit request =>
    changePassword2Form.bindFromRequest().fold(
      formWithErrors => {
        // Varidationエラーがあった場合Viewに戻ってエラー表示
        Future.successful(BadRequest(viewsAuth.changePassword(formWithErrors)))
      },
      passwords => {
        var userId = "" // パスワードを変更するユーザID
        var currentPassword = "" // パスワードを変更するユーザの現在のパスワード
        var newPassword = "" // 新しいパスワード
        if (passwords.selectUser == "dev") { // 開発管理者の場合
          userId = passwords.devUser
          currentPassword = passwords.devUserCurrent
          newPassword = passwords.devUserpassword1
        }
        if (passwords.selectUser == "sys") { // システム管理者の場合
          userId = passwords.sysUser
          currentPassword = passwords.sysUserCurrent
          newPassword = passwords.sysUserpassword1
        }
        // 現在のパスワードと入力された現在のパスワードが合っているか確認し
        credentialsProvider.authenticate(
          Credentials(
            userId,
            currentPassword
          )
        ).flatMap {
          loginInfo => {
            if (request.identity.email == userId) {
              for {
                _ <- authInfoRepository.update(
                  loginInfo,
                  passwordHasherRegistry.current.hash(newPassword)
                )
                authenticator <- env.authenticatorService.create(loginInfo)
                result <-
                  env.authenticatorService.renew(
                    authenticator,
                    Redirect(routes.Auth.changePassword)
                      .flashing("success" -> Messages("success.changePassword.devUser.update")))
              } yield result
            } else {
              for {
                _ <- authInfoRepository.update(
                  loginInfo,
                  passwordHasherRegistry.current.hash(newPassword)
                )
                result <- {
                  Future.successful(Redirect(routes.Auth.changePassword)
                    .flashing("success" -> Messages("success.changePassword.sysUser.update")))
                }
              } yield result
            }
          }
        }.recover {
          case e: ProviderException => {
            // 例外発生時は普通に元の画面に戻す
            var passForm = changePassword2Form.fill(
              ChangePassword2Form.apply(
                selectUser = "sys",
                devUser = userService.findDevUser().get.email,
                sysUser = userService.findSysUser().get.email
              )
            )
            BadRequest(viewsAuth.changePassword(passForm
              .withGlobalError(Messages("error.changePassword.password.current.notequal"))))
          }
        }
      }
    )
  }
}
