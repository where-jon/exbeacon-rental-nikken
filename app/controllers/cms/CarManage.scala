package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * 作業車管理アクションクラス
  *
  *
  */
@Singleton
class CarManage @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val dataList = Seq[String](
       "001,高所作業車A,110,100"
      ,"002,高所作業車B,220,200"
      ,"003,高所作業車C,330,300"
      ,"004,高所作業車D,440,400"
      ,"005,高所作業車F,550,500"
      ,"006,高所作業車G,660,600"
      ,"007,高所作業車H,770,700"
      ,"008,高所作業車I,880,800"
      ,"009,高所作業車J,990,900"
      ,"010,高所作業車K,AA0,A00"
      ,"011,高所作業車L,BB0,B00"
      ,"013,高所作業車M,CC0,C00"
      ,"014,高所作業車N,DD0,D00"
      ,"015,高所作業車O,EE0,E00"
      ,"016,高所作業車P,FF0,F00"
      ,"017,高所作業車R,GG0,G00"
      ,"018,高所作業車S,HH0,H00"
      ,"019,高所作業車T,II0,I00"
      ,"020,高所作業車U,JJ0,J00"
      ,"021,高所作業車V,KK0,K00"
      )
    Ok(views.html.cms.carManage(dataList))
  }
}
