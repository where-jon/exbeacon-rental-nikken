package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * BeaconTxタグ管理アクションクラス
  *
  *
  */
@Singleton
class TxTagManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val dataList = Seq[String](
       "101,100,作業車鍵,高所作業車A,　"
      ,"102,0,仮設材,ペガサスLL,仮設材管理No.1"
      ,"103,90,作業車,高所作業車B,　"
      ,"104,10,仮設材,ラクサー,仮設材管理No.2"
      ,"105,80,仮設材,ペガサスM,仮設材管理No.3"
      ,"106,20,作業車,高所作業車C,　"
      ,"107,70,仮設材,便利棚,仮設材管理No.4"
      ,"108,30,作業車,高所作業車D,　"
      ,"109,60,作業車,高所作業車E,　"
      ,"110,40,作業車,高所作業車F,　"
      ,"111,50,作業車,高所作業車G,　"
      ,"112,1,作業車,高所作業車H,　"
      ,"113,99,作業車,高所作業車I,　"
      ,"114,5,作業車,高所作業車J,　"
      ,"115,95,作業車,高所作業車K,　"
      ,"116,30,作業車,高所作業車L,　"
      ,"117,31,作業車,高所作業車M,　"
      ,"118,100,作業車,高所作業車N,　"
      ,"119,100,作業車,高所作業車O,　"
      ,"120,100,作業車,高所作業車P,　"
      ,"121,100,作業車,高所作業車Q,　"
    )
    Ok(views.html.cms.txTagManage(dataList))
  }
}
