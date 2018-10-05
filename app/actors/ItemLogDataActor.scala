package actors

import javax.inject.Inject

import akka.actor.Actor
import controllers.BeaconService
import models._
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}


/**
  * itemLogテーブルデータ保存バッチ
  *
  */
class ItemLogDataActor @Inject()(config: Configuration
  , ws: WSClient
  , itemlogDAO: ItemLogDAO
  , beaconDAO: models.beaconDAO
  , beaconService: BeaconService
  , placeDAO: placeDAO
  ) extends Actor {
  val BATCH_NAME = "仮設材記録"
  var dbDatas :Seq[BeaconViewer] = null; // 仮設材種別
  var beaconData : itemLogPositionData = null;
  private val enableLogging = config.getBoolean("akka.quartz.schedules.ItemLogDataActor.loggingStart").getOrElse(false)
  val WORKING_STATUS = 1  // 施行中
  def receive = {
    case msg:String => {
      execute()
    }
  }

  /**
    * 実際の処理
    */
  private def execute():Unit = {
    if (enableLogging) {
      // 処理開始
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)
      try{
        val placeData = placeDAO.selectPlaceAll(WORKING_STATUS)
        placeData.zipWithIndex.map { case (place, i) =>
          if(place.btxApiUrl != null && place.btxApiUrl != ""){
            val placeId = place.placeId
            if(beaconService.getCloudUrl(placeId)){
              dbDatas = beaconDAO.selectBeaconViewer(placeId)
              val beaconListApi = beaconService.getItemLogPosition(dbDatas,true,placeId)
              if(beaconListApi!= null) {
                beaconListApi.zipWithIndex.map { case (beacon, i) =>
                  beaconData = beacon
                  itemlogDAO.insert(beacon)
                }
              }else{
                System.out.println("--------------------現場apiと登録データ違い.start--------------------------")
                System.out.println("dbDatas:::" + dbDatas)
                System.out.println("--------------------現場apiと登録データ違い.end--------------------------")
              }
            }else{
              System.out.println("--------------------現場api情報ない.start--------------------------")
              System.out.println("dbDatas:::" + dbDatas)
              System.out.println("--------------------現場api情報ない.end--------------------------")
            }
          }
        }
      }catch{
        case e: Exception =>
          System.out.println("--------------------バッチエラー検知.start--------------------------")
          System.out.println("dbDatas:::" + dbDatas)
          System.out.println("beaconData:::" + beaconData)
          Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
          System.out.println("--------------------バッチエラー検知.end--------------------------")
      }
    }

  }
}