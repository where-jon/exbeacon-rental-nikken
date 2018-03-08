package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.FloorSummery
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}

/**
  * 高所作業車予約アクションクラス
  *
  *
  */
@Singleton
class CarReserve @Inject()(config: Configuration
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
    val floorSummeryDataList = Seq[FloorSummery](
        FloorSummery("8","","")
      , FloorSummery("7","","")
      , FloorSummery("6","","")
      , FloorSummery("5","","2")
      , FloorSummery("4","7","7")
      , FloorSummery("3","5","5")
      , FloorSummery("2","2","1")
      , FloorSummery("1","","")
    )
    Ok(views.html.carReserve(companyList, floorList, floorSummeryDataList))
  }

}
