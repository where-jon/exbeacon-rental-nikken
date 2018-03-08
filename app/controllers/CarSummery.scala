package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette

import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * 高所作業車各階稼働状況アクションクラス
  *
  *
  */
@Singleton
class CarSummery @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val companyList = Seq[String](
      "ダクト1"
      ,"ダクト2"
      ,"配管1"
      ,"配管2"
      ,"保温1"
      ,"保温2"
      ,"計装1"
      ,"計装2"
      ,"多能1")
    val floorList = Seq[String](
      "8"
      ,"7"
      ,"6"
      ,"5"
      ,"4"
      ,"3"
      ,"2"
      ,"1")

    Ok(views.html.carSummery(companyList, floorList))
  }

}
