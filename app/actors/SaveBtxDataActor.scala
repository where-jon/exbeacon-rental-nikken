package actors

import javax.inject.Inject

import akka.actor.Actor
import models._
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.collection.immutable.List


/**
  * BTX最新位置情報保存バッチ
  *
  */
class SaveBtxDataActor @Inject()(    config: Configuration
                                     , ws: WSClient
                                     , placeDAO: placeDAO
                                     , floorDAO: floorDAO
                                     , btxLastPositionDAO: btxLastPositionDAO
                                ) extends Actor {
  val BATCH_NAME = "BTX最新位置情報保存バッチ"

  def receive = {
    case msg:String => {
      execute()
    }
  }

  /**
    * 実際の処理
    */
  private def execute():Unit = {
    // 処理開始
    Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)

    try{

      val placeList = placeDAO.selectPlaceList()
      // 現場毎に処理
      for(place <- placeList){// for - start
        // 登録リスト
        var inputHistoryList = Seq[BtxLastPosition]()

        // API通信実行
        if(place.btxApiUrl.nonEmpty){
          Logger.info(s""" ${BATCH_NAME} _ ${place.placeName} を処理""")

          // API呼び出し
          ws.url(place.btxApiUrl).get().map { response =>
            // フロア情報
            val floorInfoList = floorDAO.selectFloorInfo(place.placeId)
            // APIからのデータ
            val apiDataList = Json.parse(response.body).asOpt[List[models.exCloudBtxData]].getOrElse(Nil)

            // 履歴のインプットを貯める
            apiDataList.foreach{d =>
              val floors = floorInfoList.filter(_.exbDeviceIdList contains d.device_id.toString)
              if(floors.nonEmpty){
                inputHistoryList :+= BtxLastPosition(d.btx_id, place.placeId, floors.last.floorId)
              }
            }
            // 履歴の登録
            btxLastPositionDAO.update(inputHistoryList)

          }recover {
            case e: Exception => Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
          }
        }else{
          Logger.warn(s""" API_URL未設定：現場ID：${place.placeId} """)
        }
      }// for - end

      // 処理終了
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} -- end -- """)

    }catch{
      case e: Exception => Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
    }
  }
}