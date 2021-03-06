package models

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import javax.inject.Inject
import anorm.SqlParser._
import anorm.{~, _}
import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}


/*作業車稼働状況分析用クラス*/
case class WorkRate(
itemId:Int,
itemTagId:Int,
itemKeyTagId:Int,
itemNo:String,
itemName:String
,operatingRate : Float
,reserveOperatingRate :Float
,operatingCount :Int
,reserveOperatingCount :Int
)

/*作業車稼働状況分析用クラス*/
case class WeekData(
szYobi :String,
iNum :Int,
iWeekStartDay :String,
iWeekEndDay :String,
iWeekTotalWorkDay :Int,
iWeekRealWorkDay :Int,
iWeekTotalTime :Int
)


/*作業車稼働状況分析検索用formクラス*/
case class MovementCarSearchData(
  inputDate: String
)

case class MovementWorkingLog(
item_car_id: Int
,item_car_name: String
,item_car_btx_id: Int
,detected_count: Int
,operating_rate: Int
,reserve_operating_rate: Int
)

object MovementWorkingLog {
  implicit val jsonReads: Reads[MovementWorkingLog] = (
    (JsPath \ "item_car_id").read[Int] ~
      ((JsPath \ "item_car_name").read[String] | Reads.pure("")) ~
      (JsPath \ "item_car_btx_id").read[Int] ~
      (JsPath \ "detected_count").read[Int] ~
      (JsPath \ "operating_rate").read[Int] ~
      ((JsPath \ "reserve_operating_rate").read[Int])
    )(MovementWorkingLog.apply _)

  implicit def jsonWrites = Json.writes[MovementWorkingLog]
}

/*未検出の仮設材検索用formクラス*/
case class UnDetectedSearchData(
  itemTypeId: Int,
  floorName: String,
  inputDate: String
)

case class DetectLog(
  item_id: Int
  ,item_btx_id: Int
  ,detect_count: Int
)
case class ItemLog(
item_id: Int
,item_btx_id: Int
,finish_floor_name: String
,finish_exb_name: String
,finish_detected_time: String
,company_name: String
,item_type_id: Int
,item_type_name: String
,item_name: String
)

object ItemLog {
  implicit val jsonReads: Reads[ItemLog] = (
    (JsPath \ "item_id").read[Int] ~
      (JsPath \ "item_btx_id").read[Int] ~
      ((JsPath \ "finish_floor_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "finish_exb_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "finish_detected_time").read[String] | Reads.pure("")) ~
      ((JsPath \ "company_name").read[String]| Reads.pure("")) ~
      (JsPath \ "item_type_id").read[Int] ~
      ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_name").read[String] | Reads.pure(""))
    )(ItemLog.apply _)

  implicit def jsonWrites = Json.writes[ItemLog]
}

case class ItemLogDeleteDate(
  place_id: Int,
  updatetime: String
)

@javax.inject.Singleton
class ItemLogDAO @Inject() (dbapi: DBApi) {
  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("item_id") ~
      get[Int]("item_btx_id") ~
      get[String]("finish_floor_name") ~
      get[String]("finish_exb_name") ~
      get[String]("finish_detected_time") ~
      get[String]("company_name") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("item_name")  map {
      case item_id ~ item_btx_id ~ finish_floor_name ~
        finish_exb_name ~ finish_detected_time ~ company_name ~
        item_type_id ~ item_type_name ~ item_name =>
        ItemLog(item_id, item_btx_id, finish_floor_name,
          finish_exb_name, finish_detected_time, company_name,
          item_type_id,item_type_name, item_name)
    }
  }

  def insert(itemLogData: itemLogPositionData): Boolean = {
    var vStartData =itemLogData.reserve_start_date
    var vEndData = itemLogData.reserve_end_date
    var vUpdateData = itemLogData.updatetime
    val vWorkType = itemLogData.work_type_name
    var vReserveFlg = false
    val vWorkingFlg = itemLogData.work_flg

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt

    if(vStartData == "date"){
      vStartData = null
    }else{ // 予約時間がある場合
      if(mTime >= vStartData && mTime <= vEndData){ // 予約して使ってるかをチェック
        if(mHour <= 12 && vWorkType =="終日" )
          vReserveFlg = true
        else if(mHour <= 12 && vWorkType =="終日" )
          vReserveFlg = true
        else if(mHour <= 24 && vWorkType =="午後" )
          vReserveFlg = true
        else if(mHour <= 24 && vWorkType =="終日" )
          vReserveFlg = true
      }
    }
    if(vEndData == "date"){
      vEndData = null
    }
    if(vUpdateData == "" || vUpdateData == "no"){
      vUpdateData = "0001-01-01 00:00:00"
    }

//    System.out.println("---------------------------------")
//    System.out.println("place_id:" + itemLogData.place_id)
//    System.out.println("finish_floor_name:" + itemLogData.cur_pos_name)
//    System.out.println("finish_exb_name:" + itemLogData.cur_exb_name)
//    System.out.println("item_id:" + itemLogData.item_id)
//    System.out.println("item_type_id:" + itemLogData.item_type_id)
//    System.out.println("item_name:" + itemLogData.item_name)
//    System.out.println("item_btx_id:" + itemLogData.item_btx_id)
//    System.out.println("item_car_key_btx_id:" + itemLogData.item_key_btx)
//    System.out.println("reserve_flg:" + vReserveFlg)
//    System.out.println("reserve_start_date:" + vStartData)
//    System.out.println("reserve_end_date:" +vEndData)
//    System.out.println("working_flg:" + vWorkingFlg)
//    System.out.println("finish_floor_id:" +  -1)
//    System.out.println("finish_exb_id:" + itemLogData.pos_id)
//    System.out.println("finish_updatetime:" + vUpdateData)
//    System.out.println("company_id:" + -1)
//    System.out.println("company_name:" + itemLogData.company_name)
//    System.out.println("---------------------------------")

    db.withConnection { implicit connection =>
      val sql = SQL("""
                    INSERT INTO item_log
                    (item_type_id, item_id,
                    item_name, item_btx_id, item_car_key_btx_id,
                    reserve_flg, reserve_start_date, reserve_end_date,
                    working_flg, finish_floor_id, finish_floor_name, finish_exb_id, finish_exb_name, finish_updatetime,
                    company_id, company_name, place_id, updatetime)
                      values
                      ({item_type_id}, {item_id},
                       {item_name}, {item_btx_id}, {item_car_key_btx_id},
                       {reserve_flg}, to_date({reserve_start_date}, 'YYYY-MM-DD'), to_date({reserve_end_date}, 'YYYY-MM-DD'),
                       {working_flg},{finish_floor_id}, {finish_floor_name}, {finish_exb_id}, {finish_exb_name}, TO_TIMESTAMP({finish_updatetime}, 'YYYY-MM-DD HH24:MI:SS'),
                       {company_id},{company_name}, {place_id}, now());

        """
      ).on(
        "finish_floor_name" ->itemLogData.cur_pos_name,
        "finish_exb_name" -> itemLogData.cur_exb_name,
        "item_id" -> itemLogData.item_id,
        "item_type_id" -> itemLogData.item_type_id,
        "item_name" -> itemLogData.item_name,
        "item_btx_id" -> itemLogData.item_btx_id,
        "item_car_key_btx_id" -> itemLogData.item_key_btx,
        "reserve_flg" -> vReserveFlg,
        "reserve_start_date" -> vStartData,
        "reserve_end_date" -> vEndData,
        "working_flg" -> vWorkingFlg,
        "finish_floor_id" -> -1,
        "finish_exb_id" -> itemLogData.pos_id,
        "finish_updatetime" -> vUpdateData,
        "place_id" -> itemLogData.place_id,
        "company_id" -> -1,
        "company_name" -> itemLogData.company_name
      )

      sql.execute
    }
  }

  def selectDetectedData(placeId: Int, detectDate: String): Seq[DetectLog] = {

    // Parser
    val simple2 = {
      get[Int]("item_id") ~
        get[Int]("item_btx_id") ~
        get[Int]("detect_count")map {
        case item_id ~ item_btx_id ~ detect_count
           => DetectLog(item_id, item_btx_id, detect_count)
      }
    }

    db.withConnection { implicit connection =>
      val sql = SQL(
        """
        select
          T1.item_id
          ,T1.item_btx_id
          ,count(T1.item_btx_id) as detect_count
          from item_log T1
          where T1.finish_updatetime
          between to_date({finish_updatetime}, 'YYYY-MM-DD') - 1 and to_date({finish_updatetime}, 'YYYY-MM-DD')
          and T1.place_id = {place_id}
           group by
          T1.item_id
          ,T1.item_btx_id
        order by T1.item_btx_id

        """
      ).on(
        'place_id -> placeId,
        'finish_updatetime -> detectDate
      )
      sql.as(simple2.*)
    }
  }

  def selectUnDetectedData(placeId: Int, detectDate: String,detectedTxList:Seq[Int]): Seq[ItemLog] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          select
          DISTINCT T1.item_btx_id
          ,T1.item_id
          ,T1.finish_floor_name
          ,T1.finish_exb_name
          ,to_char(T1.finish_updatetime, 'YYYY-MM-DD HH24:MI:SS') as finish_detected_time
          ,T1.company_name
          ,itemType.item_type_id
          ,itemType.item_type_name
          ,T1.item_name
          from item_log as T1
              INNER JOIN item_type as itemType on itemType.item_type_id = T1.item_type_id
            INNER JOIN (
             select
              item_btx_id as F1
              ,MAX(finish_updatetime) AS F2
             FROM
              item_log
              where not finish_updatetime >= to_date({finish_updatetime}, 'YYYY-MM-DD') - 1
              GROUP BY item_btx_id ) AS T2
            ON T2.F1=T1.item_btx_id AND T2.F2=T1.finish_updatetime
            where T1.place_id = {place_id}
            and finish_floor_name != '不在'
            and item_btx_id not in( """ + {detectedTxList.mkString(",")} +""" )
            and not T1.updatetime >= to_date({finish_updatetime}, 'YYYY-MM-DD') - 1

        ORDER BY
        item_btx_id

        """
      ).on(
        'place_id -> placeId,
        'finish_updatetime -> detectDate
      )
      sql.as(simple.*)
    }
  }

  // Parser
  val workingSimple = {
    get[Int]("item_car_id") ~
      get[String]("item_car_name") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("detected_count") ~
      get[Int]("operating_rate") ~
      get[Int]("reserve_operating_rate")  map {
      case item_car_id ~ item_car_name ~ item_car_btx_id ~ detected_count~
        operating_rate~ reserve_operating_rate=>
        MovementWorkingLog(item_car_id, item_car_name, item_car_btx_id, detected_count
        ,operating_rate,reserve_operating_rate)
    }
  }

  def selectWorkingOn(workingFlg: Boolean,itemCarId: Int,placeId: Int, startDate: String, endDate: String,itemIdList :Seq[Int]): Seq[MovementWorkingLog] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
        select
       itemCar.item_car_id
       ,itemCar.item_car_name
       ,itemCar.item_car_btx_id
       ,count(itemLog.item_btx_id) as detected_count
       ,coalesce(-1) as operating_rate
        ,coalesce(-1) as reserve_operating_rate
       from
       item_car_master as itemCar
        left join item_log as itemLog on itemLog.item_btx_id = itemCar.item_car_btx_id
        and itemLog.working_flg =  """ + {workingFlg}+ """
        and itemLog.place_id = """ + {placeId}+ """
        and itemLog.finish_floor_name != '不在'
        and itemLog.finish_updatetime
        between to_date('""" + {startDate}+ """', 'YYYY-MM-DD') and  TO_TIMESTAMP('""" + {endDate}+ """', 'YYYY-MM-DD') + '1 day'
       where itemCar.place_id = """ + {placeId}+ """
       and itemCar.active_flg = true
       and itemCar.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
       and itemCar.item_car_id = """ + {itemCarId}+ """
       group by
       itemCar.item_car_id
       ,itemCar.item_car_name
       ,itemLog.item_btx_id
       ,itemLog.working_flg
       order by itemCar.item_car_id
        """
      )
      sql.as(workingSimple.*)
    }
  }

  def selectReserveAndWorkingOn(reserveFlg: Boolean,workingFlg: Boolean,itemCarId: Int,placeId: Int, startDate: String, endDate: String,itemIdList :Seq[Int]): Seq[MovementWorkingLog] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
        select
       itemCar.item_car_id
       ,itemCar.item_car_name
       ,itemCar.item_car_btx_id
       ,count(itemLog.item_btx_id) as detected_count
       ,coalesce(-1) as operating_rate
        ,coalesce(-1) as reserve_operating_rate
       from
       item_car_master as itemCar
        left join item_log as itemLog on itemLog.item_btx_id = itemCar.item_car_btx_id
        and itemLog.working_flg =  """ + {workingFlg}+ """
        and itemLog.reserve_flg = """ + {reserveFlg}+ """
        and itemLog.place_id = """ + {placeId}+ """
        and itemLog.finish_floor_name != '不在'
        and itemLog.finish_updatetime
        between to_date('""" + {startDate}+ """', 'YYYY-MM-DD') and  TO_TIMESTAMP('""" + {endDate}+ """', 'YYYY-MM-DD') + '1 day'
       where itemCar.place_id = """ + {placeId}+ """
       and itemCar.active_flg = true
       and itemCar.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
       and itemCar.item_car_id = """ + {itemCarId}+ """
       group by
       itemCar.item_car_id
       ,itemCar.item_car_name
       ,itemLog.item_btx_id
       ,itemLog.working_flg
       order by itemCar.item_car_id
        """
      )
      sql.as(workingSimple.*)
    }
  }

  // Parser
  val DeleteworkingSimple = {
    get[Int]("place_id") ~
      get[String]("updatetime")  map {
      case place_id ~ updatetime=>
        ItemLogDeleteDate(place_id, updatetime)
    }
  }

  def selectOldestRow(placeId: Int, deltedate: String): Seq[ItemLogDeleteDate] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          select
            log.place_id as place_id,
            to_char(max(log.updatetime), 'YYYY-MM-DD HH24:MI:SS') as updatetime
          from
            item_log as log
          where
            log.place_id = """ + {placeId}+ """
            and log.updatetime < TO_TIMESTAMP('""" + {deltedate}+ """', 'YYYY/MM/DD HH24:MI:SS')
          group by log.place_id
        """
      )
      sql.as(DeleteworkingSimple.*)
    }
  }


  def selectOldestRow(placeId: Int): Seq[ItemLogDeleteDate] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          select
            log.place_id as place_id,
            to_char(min(log.updatetime), 'YYYY-MM-DD HH24:MI:SS') as updatetime
          from
            item_log as log
          where
            log.place_id = """ + {placeId}+ """
          group by log.place_id
        """
      )
      sql.as(DeleteworkingSimple.*)
    }
  }

  /**
    * 仮設材ログ削除
    * @return
    */
  def delete(placeId: Int, deltedate: String): Unit = {
    db.withTransaction { implicit connection =>

      // 作業車の削除
      SQL(
        """
           delete from item_log
           where place_id = {placeId}
           and updatetime < TO_TIMESTAMP({deltedate}, 'YYYY/MM/DD HH24:MI:SS');
        """
        .stripMargin).on('placeId -> placeId, 'deltedate -> deltedate).executeUpdate()

      // コミット
      connection.commit()
    }
  }
}

