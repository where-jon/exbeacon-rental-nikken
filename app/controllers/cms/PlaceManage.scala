package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.core.routing.Route
import utils.silhouette.{AuthController, MyEnv}


/**
  * 現場管理アクションクラス
  *
  *
  */
@Singleton
class PlaceManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val placeNameList = Seq[String](
       "東北医科薬科大学病院"
      ,"仙台〇〇〇〇〇"
      ,"弘前〇〇〇〇〇"
      ,"八戸〇〇〇〇〇"
      ,"五所川原〇〇〇〇〇"
      ,"盛岡〇〇〇〇〇"
      ,"花巻〇〇〇〇〇"
      ,"大館〇〇〇〇〇"
      ,"秋田〇〇〇〇〇"
      ,"仙台〇〇〇〇〇"
//      ,"弘前〇〇〇〇〇"
//      ,"八戸〇〇〇〇〇"
//      ,"五所川原〇〇〇〇〇"
//      ,"盛岡〇〇〇〇〇"
//      ,"花巻〇〇〇〇〇"
//      ,"大館〇〇〇〇〇"
//      ,"秋田〇〇〇〇〇"
      )
    Ok(views.html.cms.placeManage(placeNameList))
  }

  /** 詳細 */
  def detail = SecuredAction { implicit request =>
    val placeNameList = Seq[String](
      "東北医科薬科大学病院"
    )
    Ok(views.html.cms.placeManageDetail(placeNameList))
  }

}
