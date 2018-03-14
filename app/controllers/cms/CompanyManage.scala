package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.{AuthController, MyEnv}


/**
  * 業者管理アクションクラス
  *
  *
  */
@Singleton
class CompanyManage @Inject()(config: Configuration
                              , val silhouette: Silhouette[MyEnv]
                              , val messagesApi: MessagesApi
                               ) extends AuthController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val dataList = Seq[String](
       "1,ダクト1,XXXXXXXXXXXXXXXXXXXXXX"
      ,"2,ダクト2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"3,配管1,XXXXXXXXXXXXXXXXXXXXXX"
      ,"4,配管2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"5,保温1,XXXXXXXXXXXXXXXXXXXXXX"
      ,"6,保温2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"7,計装1,XXXXXXXXXXXXXXXXXXXXXX"
      ,"8,計装2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"9,多能1,XXXXXXXXXXXXXXXXXXXXXX"
      ,"10,多能2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"11,ダクト2,XXXXXXXXXXXXXXXXXXXXXX"
      ,"12,ダクト3,XXXXXXXXXXXXXXXXXXXXXX"
      ,"13,ダクト4,XXXXXXXXXXXXXXXXXXXXXX"
      ,"14,ダクト5,XXXXXXXXXXXXXXXXXXXXXX"
      ,"15,ダクト6,XXXXXXXXXXXXXXXXXXXXXX"
      ,"16,ダクト7,XXXXXXXXXXXXXXXXXXXXXX"
      ,"17,ダクト8,XXXXXXXXXXXXXXXXXXXXXX"
      ,"18,ダクト9,XXXXXXXXXXXXXXXXXXXXXX"
      ,"19,ダクト10,XXXXXXXXXXXXXXXXXXXXXX"
      ,"20,ダクト11,XXXXXXXXXXXXXXXXXXXXXX"
      ,"21,ダクト12,XXXXXXXXXXXXXXXXXXXXXX"
    )
    Ok(views.html.cms.companyManage(dataList))
  }
}
