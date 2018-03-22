package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.{BtxTelemetryInfo, exCloudBtxData}
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
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
    val placeIdStr = super.getCurrentPlaceIdStr
    // URLを取得
    val url = placeDAO.selectPlaceList(Seq[Int](placeIdStr.toInt)).last.btxApiUrl
    // DBからの情報を取得
    val dbInfo = btxDAO.selectForTelemetry(placeIdStr.toInt)

    // API呼び出し
    ws.url(url).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      list.foreach(apiData =>{
        val dbRecords = dbInfo.filter(_.btxId == apiData.btx_id)
        if(dbRecords.length > 0){
          resultList :+= BtxTelemetryInfo(
               dbRecords.last.btxId
            ,  apiData.power_level
            ,  dbRecords.last.kindName
            ,  dbRecords.last.name
            ,  dbRecords.last.note
            ,  (apiData.updatetime.isEmpty == false)
          )
        }else{
          resultList :+= BtxTelemetryInfo(
              apiData.btx_id
            , apiData.power_level
            , ""
            , ""
            , ""
            , (apiData.updatetime.isEmpty == false)
          )
        }
      })
      Ok(views.html.cms.txTagManage(resultList, placeIdStr.toInt))
    }
  }
}
