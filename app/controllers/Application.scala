package controllers


import utils.silhouette._
import com.mohiva.play.silhouette.api.Silhouette
import play.api._

import play.api.i18n.{ MessagesApi }
import play.api.libs.ws._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import javax.inject.{ Inject, Singleton }


@Singleton
class Application @Inject() (  config: Configuration
                             , val silhouette: Silhouette[MyEnv]
                             , val messagesApi: MessagesApi
                             ) extends AuthController {

  def index = SecuredAction { implicit request =>
    Future {
    }
    Redirect(routes.CarSummery.index)
  }
  def myAccount = SecuredAction { implicit request =>
    Ok(views.html.myAccount())
  }

}