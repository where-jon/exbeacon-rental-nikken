package controllers


import utils.silhouette._
import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.MessagesApi

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import javax.inject.{Inject, Singleton}


@Singleton
class Application @Inject() (  config: Configuration
                             , val silhouette: Silhouette[MyEnv]
                             , val messagesApi: MessagesApi
                             ) extends BaseController {

  def index = SecuredAction { implicit request =>
    if(securedRequest2User.placeId != None){
      Redirect(s"""${routes.CarSummery.index.path()}?${KEY_PLACE_ID}=${securedRequest2User.placeId.get}""")
    }else{
      Redirect(cms.routes.PlaceManage.index)
    }
  }

  def myAccount = SecuredAction { implicit request =>
    Ok(views.html.myAccount())
  }

}