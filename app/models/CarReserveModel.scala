package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._


case class CarReserveModelPlotInfo(
    floorIdStr: String = ""
  , companyIdStr: String = ""
  , carIdStr: String = ""
  , carNo: String = ""
  , reserveIdStr: String = ""
  , dataBefore: String = ""
)

case class CarReserveSummeryInfo(
   floorId: Int
 , floorName: String = ""
 , carExistCount: Int = 0
 , reserveCount: Int = 0
)

// -----------------------------
case class CarReservePostJsonRequestObj(
   reserveId: Int,
   carId: Int,
   floorId: Int,
   companyId: Int,
   reserveDate: String
 )
object CarReservePostJsonRequestObj {

  implicit val jsonReads: Reads[CarReservePostJsonRequestObj] = (
    ((JsPath \ "reserveId").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "carId").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "floorId").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "companyId").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "reserveDate").read[String] | Reads.pure(""))
    )(CarReservePostJsonRequestObj.apply _)

  implicit def jsonWrites = Json.writes[CarReservePostJsonRequestObj]
}
case class CarReservePostJsonResponseObj(
    result: Boolean
  , reserveId: Int = 0
)

object CarReservePostJsonResponseObj {
  implicit def jsonWrites = Json.writes[CarReservePostJsonResponseObj]
}

@javax.inject.Singleton
class carReserveDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 予約情報の取得
    * @return
    */
  def selectReserveForPlot(placeId: Int, dateStr:String): Seq[CarReserveModelPlotInfo] = {

    val dateStrTomorrow = dateStr

    val simple = {
        get[Int]("reserve_id") ~
        get[Int]("car_id") ~
        get[String]("car_no") ~
        get[Int]("floor_id") ~
        get[Int]("company_id") map {
        case reserve_id ~ car_id  ~ car_no  ~ floor_id ~ company_id  =>
          CarReserveModelPlotInfo(
              floor_id.toString
            , company_id.toString
            , car_id.toString
            , car_no
            , reserve_id.toString
          )
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.reserve_id
            , r.car_id
            , c.car_no
            , r.floor_id
            , r.company_id
          from
            reserve_table r
            inner join car_master c
              on r.car_id = c.car_id
            inner join floor_master f
              on r.floor_id = f.floor_id
            inner join company_master cm
              on r.company_id = cm.company_id
          where
            r.active_flg = true
            and c.active_flg = true
            and f.active_flg = true
            and cm.active_flg = true
            and c.place_id = {placeId}
            and f.place_id = {placeId}
            and cm.place_id = {placeId}
            and r.reserve_date = to_date({dateStrTomorrow}, 'YYYYMMDD')
          order by
            f.floor_id
        """
      SQL(selectPh)
        .on('placeId -> placeId, 'dateStrTomorrow -> dateStrTomorrow)
        .as(simple.*)
    }
  }

}

