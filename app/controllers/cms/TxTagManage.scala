package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.{BtxTelemetryInfo, exCloudBtxData}
import play.api._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws._
import utils.silhouette.MyEnv
import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
  * BeaconTxタグ管理アクションクラス
  *
  *
  */
@Singleton
class TxTagManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                            , ws: WSClient
                            , placeDAO: models.placeDAO
                            , btxDAO: models.btxDAO
                               ) extends BaseController with I18nSupport {

  /**
    * 初期表示
    * @return
    */
  def index = SecuredAction.async { implicit request =>

    // 戻り値
    var resultList = Seq[BtxTelemetryInfo]()

    // 選択された現場の現場ID
    val placeId = super.getCurrentPlaceId
    // URLを取得
    var url = placeDAO.selectPlaceList(Seq[Int](placeId)).last.btxApiUrl
    // DBからの情報を取得
    var dbInfo = btxDAO.selectForTelemetry(placeId)

    // API呼び出し
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // APIのデータをベースにしてDBのデータを表示する
      list.foreach(apiData =>{
        val dbRecords = dbInfo.filter(_.btxId == apiData.btx_id)
        if (dbRecords.nonEmpty) {
          dbInfo = dbInfo.filter(_.btxId != apiData.btx_id)
        }
        if(dbRecords.nonEmpty){
          resultList :+= BtxTelemetryInfo(
               dbRecords.last.btxId
            ,  apiData.power_level
            ,  dbRecords.last.kindName
            ,  dbRecords.last.name
            ,  dbRecords.last.note
            ,  apiData.updatetime.nonEmpty
          )
        }else{
          resultList :+= BtxTelemetryInfo(
              apiData.btx_id
            , apiData.power_level
            , ""
            , ""
            , ""
            , apiData.updatetime.nonEmpty
            , false
          )
        }
      })

      // DBにのみに存在するデータを出力する
      dbInfo.foreach(dbOnlyData => {
        resultList :+= BtxTelemetryInfo(
          dbOnlyData.btxId
          , 0
          , dbOnlyData.kindName
          , dbOnlyData.name
          , dbOnlyData.note
          , false
        )
      })

      resultList.sortBy(_.btxId)

      if(super.isCmsLogged){
        Ok(views.html.cms.txTagManage(resultList, placeId))
      }else{
        Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
      }
    }
  }
}
