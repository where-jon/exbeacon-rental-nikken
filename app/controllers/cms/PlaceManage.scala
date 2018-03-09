package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * その他仮設材利用状況アクションクラス
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
    val itemNameList = Seq[String](
      "ラクサー"
      ,"便利棚"
      ,"ペガサスLL"
      ,"ペガサスM"
      ,"オリオン"
      ,"仮設材A"
      ,"仮設材B"
      ,"仮設材C"
      ,"仮設材D"
      ,"仮設材E"
      ,"仮設材F"
      ,"仮設材G"
      ,"仮設材H"
      ,"仮設材I"
      ,"仮設材J"
      ,"仮設材K"
      ,"仮設材L"
      )
    Ok(views.html.otherItem(itemNameList))
  }

}
