package models

import java.text.SimpleDateFormat
import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._

case class ItemLog(
                    btx_id: Int,
                    btx_record_time: String,
                    btx_name: String,
                    btx_tantou: String,
                    btx_yakushoku: String,
                    pos_id: Int,
                    pos_name: String,
                    department_name:  String,
                    sitting_status: String,
                    updatetime: String
                  )

object ItemLog {
  implicit val jsonReads: Reads[ItemLog] = (
    (JsPath \ "btx_id").read[Int] ~
      (JsPath \ "btx_record_time").read[String] ~
      ((JsPath \ "btx_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "btx_tantou").read[String] | Reads.pure("")) ~
      ((JsPath \ "btx_yakushoku").read[String] | Reads.pure("")) ~
      (JsPath \ "pos_id").read[Int] ~
      ((JsPath \ "pos_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "department_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "sitting_status").read[String] | Reads.pure("")) ~
      (JsPath \ "updatetime").read[String]
    )(ItemLog.apply _)

  implicit def jsonWrites = Json.writes[ItemLog]
}

@javax.inject.Singleton
class ItemLogDAO @Inject() (dbapi: DBApi) {
  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("btx_id") ~
      get[String]("btx_record_time") ~
      get[String]("btx_name") ~
      get[String]("btx_tantou") ~
      get[String]("btx_yakushoku") ~
      get[Int]("pos_id") ~
      get[String]("pos_name") ~
      get[String]("department_name") ~
      get[String]("sitting_status") ~
      get[String]("updatetime") map {
      case btx_id ~ btx_record_time ~ btx_name ~
        btx_tantou ~ btx_yakushoku ~ pos_id ~ pos_name ~
        department_name ~ sitting_status ~ updatetime =>
        ItemLog(btx_id, btx_record_time, btx_name,
          btx_tantou, btx_yakushoku, pos_id, pos_name,
          department_name, sitting_status, updatetime)
    }
  }

  def insert(itemLogData: itemBeaconPositionData): Boolean = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    var vStartData =itemLogData.reserve_start_date
    var vEndData = itemLogData.reserve_end_date
    var vUpdateData = itemLogData.updatetime
    if(vEndData == "date"){
      vStartData = "0001-01-01"
    }
    if(vEndData == "date"){
      vEndData = "0001-01-01"
    }
    if(vUpdateData == "" || vUpdateData == "no"){
      vUpdateData = "0001-01-01"
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
                       false, to_date({reserve_start_date}, 'YYYY-MM-DD'), to_date({reserve_end_date}, 'YYYY-MM-DD'),
                       false, -1, {finish_floor_name}, -1, {finish_exb_name}, to_date({finish_updatetime}, 'YYYY-MM-DD'),
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
        "reserve_start_date" -> vStartData,
        "reserve_end_date" -> vEndData,
        "finish_updatetime" -> vUpdateData,
        "place_id" -> itemLogData.place_id,
        "company_id" -> -1,
        "company_name" -> itemLogData.company_name
      )

      sql.execute
    }
  }

  def selectAllTime(dateBegin: String, dateEnd: String): Seq[ItemLog] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT
          A.btx_id,
          to_char(A.btx_record_time, 'YYYY-MM-DD HH24:MI:SS') as btx_record_time,
          A.btx_name,
          A.btx_tantou,
          A.btx_yakushoku,
          A.pos_id,
          A.pos_name,
          A.department_name,
          A.sitting_status,
          to_char(A.updatetime, 'YYYY-MM-DD HH24:MI:SS') as updatetime
          FROM
            btx_log as A
          WHERE
            A.btx_record_time between to_date({date_begin}, 'YYYY-MM-DD') AND to_date({date_end}, 'YYYY-MM-DD') + interval '1 day'
          ORDER BY
            btx_record_time, btx_id
        """
      ).on(
        'date_begin -> dateBegin, 'date_end -> dateEnd
      )
      sql.as(simple.*)
    }
  }
}

