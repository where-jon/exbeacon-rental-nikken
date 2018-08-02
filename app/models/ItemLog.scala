package models

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}
import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}


/*作業車動線分析検索用formクラス*/
case class MovementCarSearchData(
  inputDate: String
)

/*未検出の仮設材検索用formクラス*/
case class UnDetectedSearchData(
  itemTypeId: Int,
  floorName: String,
  inputDate: String
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
    var vWorkType = itemLogData.work_type_name
    var vReserveFlg = false
    var vWorkingFlg = itemLogData.work_flg

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

  def selectUnDetectedData(placeId: Int, detectDate: String): Seq[ItemLog] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
           select
              item.item_id
              ,item.item_btx_id
              ,item.finish_floor_name
              ,max(item.finish_exb_name) as finish_exb_name
              ,to_char(max(item.finish_updatetime), 'YYYY-MM-DD HH24:MI:SS') as finish_detected_time
              ,item.company_name
              ,itemType.item_type_id
              ,itemType.item_type_name
              ,item.item_name
              from item_log as item
              inner join item_type as itemType on itemType.item_type_id = item.item_type_id
              where item.place_id = {place_id}
              and not item.finish_updatetime >= to_date({finish_updatetime}, 'YYYY-MM-DD') - 1
              group by
              item.item_id
              ,item.item_btx_id
              ,item.finish_floor_name
              ,item.company_name
              ,itemType.item_type_id
              ,itemType.item_type_name
              ,item.item_name
              order by item.item_btx_id
        """
      ).on(
        'place_id -> placeId,
        'finish_updatetime -> detectDate
      )
      sql.as(simple.*)
    }
  }
}

