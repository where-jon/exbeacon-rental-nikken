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

/**
  * パスワード変更画面で使うFORM
  */
case class ChangePasswordForm(
  email: String = "", // ユーザID
  currentPassword: String = "", // 入力された現在のパスワード
  newPassword1: String = "", // 入力された新しいパスワード
  newPassword2: String = "" // 入力された確認用パスワード
)

/**
  * 認証関係
  */
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
  // CHANGE PASSWORD

  var changePasswordForm = Form(
    mapping(
      "email" -> nonEmptyText, // ユーザID
      "currentPassword" -> nonEmptyText, // 入力された現在のパスワード
      "newPassword1" -> passwordValidation, // 入力された新しいパスワード
      "newPassword2" -> nonEmptyText // 入力された確認用パスワード
    )
    (ChangePasswordForm.apply)
    (ChangePasswordForm.unapply)
      verifying(
        Messages("error.changePassword.password.notequal"),
        form => form.newPassword1 == form.newPassword2
      )
  )

  /**
   * Starts the change password mechanism. It shows a form to insert his current password and the new one.
   */
  def changePassword = SecuredAction { implicit request =>
    if (request.identity.level >= 4) {
      // Viewを表示
      Ok(viewsAuth.changePassword(changePasswordForm, userService.selectSuperUserList()))
    } else {
      // 権限無いとき退場
      Redirect(site.routes.WorkPlace.index())
    }
  }

  /**
   * Saves the new password and renew the cookie
   */
  def handleChangePassword = SecuredAction.async { implicit request =>
    changePasswordForm.bindFromRequest().fold(
      formWithErrors => {
        // Varidationエラーがあった場合Viewに戻ってエラー表示
        Future.successful(BadRequest(viewsAuth.changePassword(formWithErrors, userService.selectSuperUserList())))
      },
      passwords => {
        // 現在のパスワードと入力された現在のパスワードが合っているか確認し
        credentialsProvider.authenticate(
          Credentials(
            passwords.email,
            passwords.currentPassword
          )
        ).flatMap {
          loginInfo => {
            if (request.identity.email == passwords.email) {
              for {
                _ <- authInfoRepository.update(
                  loginInfo,
                  passwordHasherRegistry.current.hash(passwords.newPassword1)
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
                  passwordHasherRegistry.current.hash(passwords.newPassword1)
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
            var passForm = changePasswordForm.fill(
              ChangePasswordForm.apply(
              )
            )
            BadRequest(viewsAuth.changePassword(
              passForm.withGlobalError(Messages("error.changePassword.password.current.notequal")),
              userService.selectSuperUserList()
            ))
          }
        }
      }
    )
  }
}
