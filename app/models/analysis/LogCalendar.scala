package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}



case class GetOneWeek(
 getDay: String
)

case class LogCalendar(
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

object LogCalendar {
  implicit val jsonReads: Reads[LogCalendar] = (
    (JsPath \ "item_id").read[Int] ~
      (JsPath \ "item_btx_id").read[Int] ~
      ((JsPath \ "finish_floor_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "finish_exb_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "finish_detected_time").read[String] | Reads.pure("")) ~
      ((JsPath \ "company_name").read[String]| Reads.pure("")) ~
      (JsPath \ "item_type_id").read[Int] ~
      ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_name").read[String] | Reads.pure(""))
    )(LogCalendar.apply _)

  implicit def jsonWrites = Json.writes[LogCalendar]
}

@javax.inject.Singleton
class LogCalendarDAO @Inject()(dbapi: DBApi) {
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
        LogCalendar(item_id, item_btx_id, finish_floor_name,
          finish_exb_name, finish_detected_time, company_name,
          item_type_id,item_type_name, item_name)
    }
  }

  def selectUnDetectedData(placeId: Int, detectDate: String): Seq[LogCalendar] = {
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


  // Parser
  val simpleOneWeek = {
      get[String]("getDay")  map {
      case
       getDay =>
        GetOneWeek(getDay)
    }
  }

  def selectGetOneWeek(): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT to_CHAR(DATE_TRUNC('week', DATE_TRUNC('month', now()) + '1 week'), 'DD') as getDay
        """
      )

      sql.as(simpleOneWeek.*)
    }
  }


  def selectGetWeek(indexWeek:Int, DETECT_MONTH:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
           SELECT to_CHAR(DATE_TRUNC('week',TO_TIMESTAMP(' """ + {DETECT_MONTH}+ """ ', 'YYYY-MM-DD HH24:MI:SS')
            + ' """ + {indexWeek}+ """ week'), 'YYYY-MM-DD') as getDay

        """
      SQL(selectPh).as(simpleOneWeek.*)

    }
  }
  def selectGetLastMonthDay(DETECT_MONTH:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
           SELECT to_CHAR(DATE_TRUNC('month',TO_TIMESTAMP(' """ + {DETECT_MONTH}+ """ ', 'YYYY-MM-DD')
            + '1 month') + '-1 day', 'YYYY-MM-DD') as getDay

        """
      SQL(selectPh).as(simpleOneWeek.*)

    }
  }

  def selectGetMonth(indexWeek:Int, DETECT_MONTH:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
           SELECT to_CHAR(DATE_TRUNC('week',TO_TIMESTAMP(' """ + {DETECT_MONTH}+ """ ', 'YYYY-MM-DD HH24:MI:SS')
            + ' """ + {indexWeek}+ """ week'), 'MM') as getDay

        """
      SQL(selectPh).as(simpleOneWeek.*)

    }
  }

  def selectGetWeekMinusDay(indexWeek:Int, minusDay:Int,DETECT_MONTH:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
           SELECT to_CHAR(DATE_TRUNC('week',TO_TIMESTAMP(' """ + {DETECT_MONTH}+ """ ', 'YYYY-MM-DD HH24:MI:SS')
            + ' """ + {indexWeek}+ """ week')
                    +(' """ + {minusDay}+ """day')

        , 'YYYY-MM-DD') as getDay

        """
      SQL(selectPh).as(simpleOneWeek.*)

    }
  }


  def selectGetTermDay(indexWeek:Int,DETECT_MONTH:String,START_WEEK_DAY:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT to_CHAR(DATE_TRUNC('week', TO_TIMESTAMP('""" + {DETECT_MONTH} +"""', 'YYYY-MM-DD HH24:MI:SS') + '""" + {indexWeek}+ """ week')
          - TO_TIMESTAMP('""" + {START_WEEK_DAY} +"""', 'YYYY-MM-DD HH24:MI:SS'), 'DD') as getDay
        """
      )

      sql.as(simpleOneWeek.*)
    }
  }

  def selectGetTermStarEndDay(endDate:String,startDate:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT to_CHAR(TO_TIMESTAMP('""" + {endDate} +"""', 'YYYY-MM-DD HH24:MI:SS')
          - TO_TIMESTAMP('""" + {startDate} +"""', 'YYYY-MM-DD HH24:MI:SS') + '1 day', 'DD') as getDay
                                                  """
      )

      sql.as(simpleOneWeek.*)
    }
  }

  def selectGetGenerateDay(startDate:String,endDate:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          select to_CHAR(getDay,'YYYY-MM-DD') as getDay from
          generate_series('""" + {
          startDate
        } +
          """'::timestamp, '""" + {
          endDate
        } +
          """'::timestamp, '1 days') as getDay;
        """
      )

      sql.as(simpleOneWeek.*)
    }
  }

  def selectGetYoubi(targetDate:String): Seq[GetOneWeek] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
          select to_CHAR( EXTRACT(
          	DOW FROM TIMESTAMP '
        	""" + {targetDate} +"""
             '),'999D9')as getDay
          """
      )
      sql.as(simpleOneWeek.*)
    }
  }

}

