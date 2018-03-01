package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}

@Singleton
class CarReserve @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    var companyList = Seq[String](
       "ダクト1"
      ,"ダクト2"
      ,"配管1"
      ,"配管2"
      ,"保温1"
      ,"保温2"
      ,"計装1"
      ,"計装2"
      ,"多能1")
    var floorList = Seq[String](
       "8F"
      ,"7F"
      ,"6F"
      ,"5F"
      ,"4F"
      ,"3F"
      ,"2F"
      ,"1F")
    Ok(views.html.carReserve(companyList, floorList))
  }

}
