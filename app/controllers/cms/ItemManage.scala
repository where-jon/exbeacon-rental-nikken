package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * 仮設材管理アクションクラス
  *
  *
  */
@Singleton
class ItemManage @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val dataList = Seq[String](
       "1,ラクサー,XXXXXXXXXXXXXXXXXXXXXXXX,10"
      ,"2,ペガサスLL,XXXXXXXXXXXXXXXXXXXXXXXX,20"
      ,"3,オリオン,XXXXXXXXXXXXXXXXXXXXXXXX,30"
      ,"4,仮設材4,XXXXXXXXXXXXXXXXXXXXXXXX,40"
      ,"5,仮設材5,XXXXXXXXXXXXXXXXXXXXXXXX,50"
      ,"6,仮設材6,XXXXXXXXXXXXXXXXXXXXXXXX,60"
      ,"7,仮設材7,XXXXXXXXXXXXXXXXXXXXXXXX,70"
      ,"8,仮設材8,XXXXXXXXXXXXXXXXXXXXXXXX,80"
      ,"9,仮設材9,XXXXXXXXXXXXXXXXXXXXXXXX,90"
      ,"10,仮設材10,XXXXXXXXXXXXXXXXXXXXXXXX,100"
      ,"11,仮設材11,XXXXXXXXXXXXXXXXXXXXXXXX,110"
      ,"12,仮設材12,XXXXXXXXXXXXXXXXXXXXXXXX,120"
      ,"13,仮設材13,XXXXXXXXXXXXXXXXXXXXXXXX,130"
      ,"14,仮設材14,XXXXXXXXXXXXXXXXXXXXXXXX,140"
      ,"15,仮設材15,XXXXXXXXXXXXXXXXXXXXXXXX,150"
      ,"16,仮設材16,XXXXXXXXXXXXXXXXXXXXXXXX,160"
      ,"17,仮設材17,XXXXXXXXXXXXXXXXXXXXXXXX,170"
      ,"18,仮設材18,XXXXXXXXXXXXXXXXXXXXXXXX,180"
      ,"19,仮設材19,XXXXXXXXXXXXXXXXXXXXXXXX,190"
      )
    Ok(views.html.cms.itemManage(dataList))
  }


}
