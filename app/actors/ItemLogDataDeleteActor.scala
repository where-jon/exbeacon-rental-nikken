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
  * itemL_log及び、reserve_tableテーブルデータ削除バッチ
  *
  */
class ItemLogDataDeleteActor @Inject()(config: Configuration
                                       , ws: WSClient
                                       , userDAO: UserDAO
                                       , itemlogDAO: ItemLogDAO
                                       , reserveMasterDAO: ReserveMasterDAO
                                       , placeDAO: placeDAO
  ) extends Actor {
  val BATCH_NAME = "仮設材削除"
  val WORKING_STATUS = 1  // 施行中
  private val enableLogging = config.getBoolean("akka.quartz.schedules.ItemLogDataDeleteActor.logDeleteStart").getOrElse(false)
  private val deleteInterval = config.getInt("akka.quartz.schedules.ItemLogDataDeleteActor.logDeleteInterval").getOrElse(0)
  private val deletionExclusionSite = config.getString("akka.quartz.schedules.ItemLogDataDeleteActor.deletionExclusionSite").getOrElse(0)

  def receive = {
    case msg:String => {
      execute()
    }
  }

  /**
    * 実際の処理
    */
  private def execute():Unit = {
    if (enableLogging && deleteInterval != 0) {
      var exclusionPlaceID = Array[String]("0")
      if(!deletionExclusionSite.toString.isEmpty){
        exclusionPlaceID = deletionExclusionSite.toString().split(",")
      }

      val df = new SimpleDateFormat("yyyy-MM-")
      // カレンダークラスのインスタンスを取得
      val cal = Calendar.getInstance
      val calDel = Calendar.getInstance
      val delMonth = (deleteInterval - 1) * -1
      calDel.add(Calendar.MONTH, delMonth); // 先月、先々月分は残す
      val delateTime = "01 00:00:00" // 1日固定
      val delateDate = df.format(calDel.getTime()).toString + delateTime

      // 削除開始対象月取得
      // 環境変数（３カ月）を減算
      val deletionPeriod = deleteInterval * -1
      cal.add(Calendar.MONTH, deletionPeriod);

      // 処理開始
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)
      try{
        import java.text.SimpleDateFormat
        // DateFormat
        val ymdf = new SimpleDateFormat("yyyyMM")
        val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        // 削除開始対象月
        val targetMonth = ymdf.format(cal.getTime())

        val sdf2 = new SimpleDateFormat("yyyy-MM-01 00:00:00")
        val targetMonth2 = sdf2.format(cal.getTime())

        val placeData = placeDAO.selectPlaceAll(WORKING_STATUS)
        placeData.zipWithIndex.foreach { case (place, i) =>
          // ログ削除除外現場チェック
          val exclusionId = exclusionPlaceID.find(_ == place.placeId.toString)
          if(place.btxApiUrl != null && place.btxApiUrl != "" && exclusionId.isEmpty){
            val placeId = place.placeId

            // 削除開始対象月以前のデータは全て削除(ゴミデータ対策)
            itemlogDAO.delete(placeId, targetMonth2)

            val itemLogMinData = itemlogDAO.selectOldestRow(placeId)
            if(itemLogMinData != null && itemLogMinData.length > 0){
              // 最古のレコード　Date型変換
              val lastMonth = new DateTime(sdf.parse(itemLogMinData.last.updatetime))
              val lastYearMonth = lastMonth.toString("yyyyMM")
              // 削除開始対象月が有るか？
              if(lastYearMonth.toInt <= targetMonth.toInt){
                // 削除対象データ有り
                val itemLogMinData = itemlogDAO.selectOldestRow(placeId, delateDate)
                if(itemLogMinData != null && itemLogMinData.length > 0){
                  // 仮設材ログ削除
                  itemlogDAO.delete(placeId, delateDate)
                  // 予約テーブル削除
                  reserveMasterDAO.batchReserveItemDelete(placeId, delateDate)
                  Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- end -- """)
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
