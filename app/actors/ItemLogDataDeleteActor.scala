package actors

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.Actor
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

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
  private val deleteInterval = config.getInt("akka.quartz.schedules.ItemLogDataDeleteActor.logDeleteInterval").getOrElse(0)

  def receive = {
    case msg:String => {
      execute()
    }
  }

  /**
    * 実際の処理
    */
  private def execute():Unit = {
    if (enableLogging || deleteInterval != 0) {
      val df = new SimpleDateFormat("yyyy-MM")
      // カレンダークラスのインスタンスを取得
      val cal = Calendar.getInstance
      var delateTime = new DateTime().toString(" HH:mm:ss")
      var delateDate = df.format(cal.getTime()) + delateTime

      // 削除開始対象月取得
      // 環境変数（３カ月）を減算
      var deletionPeriod = deleteInterval * -1
      cal.add(Calendar.MONTH, deletionPeriod);

      // 処理開始
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)
      try{
        import java.text.SimpleDateFormat
        // DateFormat
        val ymdf = new SimpleDateFormat("yyyyMM")
        val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        // 削除開始対象月
        var targetMonth = ymdf.format(cal.getTime())

        val placeData = placeDAO.selectPlaceAll()
        placeData.zipWithIndex.map { case (place, i) =>
          if(place.btxApiUrl != null && place.btxApiUrl != ""){
            val placeId = place.placeId
            val itemLogMinData = itemlogDAO.selectOldestRow(placeId)
            if(itemLogMinData != null && itemLogMinData.length > 0){
              // 最古のレコード　Date型変換
              val lastMonth = new DateTime(sdf.parse(itemLogMinData.last.updatetime))
              var lastYearMonth = lastMonth.toString("yyyyMM")
              // 削除開始対象月が有るか？
              if(lastYearMonth.toInt <= targetMonth.toInt){
                // 削除対象データ有り
                val itemLogMinData = itemlogDAO.selectOldestRow(placeId, delateDate)
                if(itemLogMinData != null && itemLogMinData.length > 0){
                  itemlogDAO.delete(placeId, delateDate)
                }
              }
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
