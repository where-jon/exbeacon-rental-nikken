package actors

import akka.actor.Actor
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import java.util.Calendar
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
  * itemLogテーブルデータ保存バッチ
  *
  */
class ItemLogDataDeleteActor @Inject()(config: Configuration
                                       , ws: WSClient
                                       , itemlogDAO: ItemLogDAO
                                       , placeDAO: placeDAO
  ) extends Actor {
  val BATCH_NAME = "仮設材削除"
  private val enableLogging = config.getBoolean("akka.quartz.schedules.ItemLogDataDeleteActor.logDeleteStart").getOrElse(false)

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
      // 削除時間
//      var delateDate = new DateTime().toString("yyyy-MM-dd 00:00:00")
//      delateDate = "2018-09-04 00:00:00"
      // DateFormat
      val df = new SimpleDateFormat("yyyy-MM-dd")
      // カレンダークラスのインスタンスを取得
      val cal = Calendar.getInstance
      // ３カ月を減算
      cal.add(Calendar.MONTH, -3);
      var delateTime = new DateTime().toString(" HH:mm:ss")
      var delateDate = df.format(cal.getTime()) + delateTime
//      var delateDate = df.format(cal.getTime()) + " 00:00:00"

      // 処理開始
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)
      try{
        val placeData = placeDAO.selectPlaceAll()
        placeData.zipWithIndex.map { case (place, i) =>
          if(place.btxApiUrl != null && place.btxApiUrl != ""){
            val placeId = place.placeId
            val itemLogMinData = itemlogDAO.selectOldestRow(placeId, delateDate)
            if(itemLogMinData != null && itemLogMinData.length > 0){
              itemlogDAO.delete(placeId, delateDate)
            }
          }
        }
      }catch{
        case e: Exception =>
          System.out.println("--------------------バッチエラー検知.start--------------------------")
          Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
          System.out.println("--------------------バッチエラー検知.end--------------------------")
      }
    }

  }
}
