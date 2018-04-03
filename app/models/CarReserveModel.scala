package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._

case class ClassColorEnum(
    RESERVE_NORMAL: String = "reserveNormal"
    , RESERVE_NONE: String = "reserveNone"
    , RESERVE_DONE: String = "reserveDone"
    , RESERVE_DUPLICATE: String = "reserveDuplicate"
    , RESERVE_DIFF: String = "reserveDiff"
)

case class CarReserveModelPlotInfo(
    floorIdStr: String = ""
  , companyIdStr: String = ""
  , carIdStr: String = ""
  , carNo: String = ""
  , reserveIdStr: String = ""
  , colorName:String = "reserveNormal"
)

case class CarReserveSummeryInfo(
   floorId: Int
 , floorName: String
 , carExistCount: Int
 , reserveCount: Int
)

@javax.inject.Singleton
class carReserveDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

//  /**
//    * 予約情報の取得
//    * @return
//    */
//  def selectReserve(floorId:Option[Int] = None, companyId:Option[Int] = None, dateStr:String = ""): Seq[ReserveInfo] = {
//
//    val simple = {
//      get[Int]("reserve_id") ~
//        get[Int]("car_id") ~
//        get[Int]("car_btx_id") ~
//        get[Int]("car_key_btx_id") ~
//        get[Int]("floor_id") ~
//        get[Int]("company_id") ~
//        get[String]("reserve_date") map {
//        case reserve_id ~ car_id ~ car_btx_id ~ car_key_btx_id ~ floor_id ~ company_id ~ reserve_date  =>
//          ReserveInfo(reserve_id, car_id, car_btx_id, car_key_btx_id, floor_id, company_id, reserve_date)
//      }
//    }
//
//    db.withConnection { implicit connection =>
//      val selectPh =
//        """
//          select
//              r.reserve_id
//            , r.car_id
//            , c.car_btx_id
//            , c.car_key_btx_id
//            , r.floor_id
//            , r.company_id
//            , to_char(r.reserve_date, 'YYYYMMDD') as reserve_date
//          from
//            reserve_table r
//            inner join car_master c
//              on r.car_id = c.car_id
//          where
//            r.active_flg = true
//        """
//      var wherePh = ""
//      if(floorId != None){
//        wherePh += s""" and r.floor_id = ${floorId.get} """
//      }
//      if(companyId != None){
//        wherePh += s""" and r.company_id = ${companyId.get} """
//      }
//      if(dateStr.isEmpty == false){
//        wherePh += s""" and r.reserve_date = to_date('${dateStr}', 'YYYYMMDD') """
//      }
//      val orderPh =
//        """
//          order by
//            r.reserve_id
//        """
//      SQL(selectPh + wherePh + orderPh).as(simple.*)
//    }
//  }
//
//
  /**
    * 予約情報の取得
    * @return
    */
  def selectReserveForPlot(placeId: Int, date:org.joda.time.DateTime): Seq[CarReserveModelPlotInfo] = {

    val dateStrToday = date.toString("yyyyMMdd")
    val dateStrYesterday = date.toString("yyyyMMdd")

    val simple = {
        get[Int]("reserve_id") ~
        get[Int]("car_id") ~
        get[String]("car_no") ~
        get[Int]("floor_id") ~
        get[Int]("company_id") ~
        get[String]("yesterday_floor_company_array")  map {
        case reserve_id ~ car_id  ~ car_no  ~ floor_id ~ company_id ~ yesterday_floor_company_array  =>
          CarReserveModelPlotInfo(
              floor_id.toString
            , company_id.toString
            , car_id.toString
            , car_no
            , reserve_id.toString
            , getClassColor(yesterday_floor_company_array, floor_id.toString+"-"+company_id.toString)
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
            , ARRAY_TO_STRING(
                ARRAY(
                  select
                    cast(floor_id as text) || '-' || cast(company_id as text)
                  from
                    reserve_table
                  where
                    car_id = r.car_id
                    and reserve_date = to_date({dateStrYesterday}, 'YYYYMMDD')
                    and active_flg = true
                )
              , ',') as yesterday_floor_company_array
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
            and r.reserve_date = to_date({dateStrToday}, 'YYYYMMDD')
          order by
            f.floor_id
        """
      val result = SQL(selectPh)
        .on('placeId -> placeId, 'dateStrToday -> dateStrToday, 'dateStrYesterday -> dateStrYesterday)
        .as(simple.*)

      result.map{ data =>
        val length = result.filter(_.companyIdStr == data.companyIdStr)
                            .filter(_.floorIdStr == data.floorIdStr)
                            .filter(_.carIdStr != data.carIdStr).length
        if(length > 0){
          CarReserveModelPlotInfo(
            data.floorIdStr
            , data.companyIdStr
            , data.carIdStr
            , data.carNo
            , data.reserveIdStr
            , ClassColorEnum().RESERVE_DUPLICATE
          )
        }else{
          data
        }
      }
    }
  }

  def getClassColor(yesterday_floor_company_array: String, today_floor_company: String) : String = {

    val seq = yesterday_floor_company_array.split(",").toSeq
    if(seq.isEmpty){
      ClassColorEnum().RESERVE_DIFF
    }else{
      val same = seq.filter(_ == today_floor_company).length
      val diff = seq.filterNot(_ == today_floor_company).length
      if(same == 0){
        ClassColorEnum().RESERVE_DIFF
      }else{
        if(diff == 0){
          ClassColorEnum().RESERVE_NORMAL
        }else{
          ClassColorEnum().RESERVE_DIFF
        }
      }
    }
  }


}

