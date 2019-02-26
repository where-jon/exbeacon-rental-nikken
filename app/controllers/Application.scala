package controllers


import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.{Inject, Singleton}
import play.api._
import play.api.i18n.MessagesApi
import utils.silhouette._


@Singleton
class Application @Inject() (  config: Configuration
                             , val silhouette: Silhouette[MyEnv]
                             , val messagesApi: MessagesApi
                             ) extends BaseController {

  def index = SecuredAction { implicit request =>
    if(securedRequest2User.isSysMng == false){
      Redirect(site.routes.ItemCarMaster.index)
    }else{
      Redirect(tenant.routes.WorkPlaceController.index)
    }
  }

  def myAccount = SecuredAction { implicit request =>
    Ok(views.html.myAccount())
  }

}
