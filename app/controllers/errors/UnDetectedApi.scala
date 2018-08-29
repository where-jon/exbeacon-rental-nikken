package controllers.errors

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.MyEnv

/**
  * API未検知エラー対応画面
  *
  *
  */

@Singleton
class UnDetectedApi @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  /** siteから遷移 */
  def indexSite = SecuredAction { implicit request =>
    Ok(views.html.errors.unDetectedApi())
  }

  /** 分析から遷移 */
  def indexAnalysis = SecuredAction { implicit request =>
    Ok(views.html.errors.unDetectedApi())
  }

}
