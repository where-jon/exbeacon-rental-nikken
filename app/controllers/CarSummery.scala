package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}

@Singleton
class CarSummery @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    Ok(views.html.carSummery("トップ"))
  }

}
