package actors

import javax.inject.Inject

import akka.actor.Actor
import controllers.BeaconService
import models._
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}


/**
  * BTX最新位置情報保存バッチ
  *
  */
class SaveBtxDataActor @Inject()(    config: Configuration
                                     , ws: WSClient
                                     , itemlogDAO: ItemLogDAO
                                     , beaconDAO: models.beaconDAO
                                     , beaconService: BeaconService
                                     , placeDAO: placeDAO
                                ) extends Actor {
  val BATCH_NAME = "仮設材記録"
  private val schedule = config.getString("akka.quartz.schedules.ExbLogActor.expression").getOrElse("NONE")
  private val enableLogging = config.getBoolean("daidanWeb.setting.logging").getOrElse(false)

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
      val placeData = placeDAO.selectPlaceAll()
      placeData.zipWithIndex.map { case (place, i) =>
        if(place.btxApiUrl != null && place.btxApiUrl != ""){
          val placeId = place.placeId
          val dbDatas = beaconDAO.selectBeaconViewer(placeId)
          val beaconListApi = beaconService.getItemLogPosition(dbDatas,true,placeId)
          beaconListApi.zipWithIndex.map { case (beacon, i) =>
            System.out.println("-------beaconData--------" + beacon)
            itemlogDAO.insert(beacon)
          }
        }
      }
    }catch{
      case e: Exception => Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
    }
  }
}