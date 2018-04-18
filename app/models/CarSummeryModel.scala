package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._

//  画面に渡す稼働情報JSONオブジェクト -------------------
case class CarSummeryWorkPlotInfo(
    floorId: String
  , carId: String
  , carNo: String
  , companyId: String = ""
  , isWorking: Boolean
)

object CarSummeryWorkPlotInfo {

  implicit def jsonWrites = Json.writes[CarSummeryWorkPlotInfo]
}

//  画面に渡す予約情報JSONオブジェクト -------------------
case class CarSummeryReservePlotInfo(
  carId: String
  , carNo: String
  , floorId: String
  , companyId: String = ""
)

object CarSummeryReservePlotInfo {

  implicit def jsonWrites = Json.writes[CarSummeryReservePlotInfo]
}

//  画面に渡すJSONオブジェクト -------------------
case class CarSummeryPlotInfo(
    workInfoList: List[CarSummeryWorkPlotInfo]
  , reserveInfoList: List[CarSummeryReservePlotInfo]
  , summeryInfoList: List[CarSummeryInfo]
  , allTotal: Int
)

object CarSummeryPlotInfo {

  implicit def jsonWrites = Json.writes[CarSummeryPlotInfo]
}
//  ----------------------------------

case class CarSummeryInfo(
    floorName: String
  , reserveCnt: Int
  , normalWorkingCnt: Int
  , workingOnlyCnt: Int
  , reserveOnlyCnt: Int
  , noReserveNoWorkingCnt: Int
)
object CarSummeryInfo {

  implicit def jsonWrites = Json.writes[CarSummeryInfo]
}


case class CarSummeryModelReserveInfo(
    reserveId: Int
  , carId: Int
  , carBtxId: Int
  , carKeyBtxId: Int
  , floorId: Int
  , companyId: Int
  , reserveDateStr: String
)

@javax.inject.Singleton
class carSummeryDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 予約情報の取得
    * @return
    */
  def selectReserve(floorId:Option[Int] = None, companyId:Option[Int] = None, dateStr:String = "", placeId:Option[Int] = None): Seq[CarSummeryModelReserveInfo] = {

    val simple = {
      get[Int]("reserve_id") ~
        get[Int]("car_id") ~
        get[Int]("car_btx_id") ~
        get[Int]("car_key_btx_id") ~
        get[Int]("floor_id") ~
        get[Int]("company_id") ~
        get[String]("reserve_date") map {
        case reserve_id ~ car_id ~ car_btx_id ~ car_key_btx_id ~ floor_id ~ company_id ~ reserve_date  =>
          CarSummeryModelReserveInfo(reserve_id, car_id, car_btx_id, car_key_btx_id, floor_id, company_id, reserve_date)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.reserve_id
            , r.car_id
            , c.car_btx_id
            , c.car_key_btx_id
            , r.floor_id
            , r.company_id
            , to_char(r.reserve_date, 'YYYYMMDD') as reserve_date
            , r.updatetime
          from
            reserve_table r
            inner join car_master c
              on r.car_id = c.car_id
          where
            r.active_flg = true
        """
      var wherePh = ""
      if(floorId != None){
        wherePh += s""" and r.floor_id = ${floorId.get} """
      }
      if(companyId != None){
        wherePh += s""" and r.company_id = ${companyId.get} """
      }
      if(dateStr.isEmpty == false){
        wherePh += s""" and r.reserve_date = to_date('${dateStr}', 'YYYYMMDD') """
      }
      if(placeId != None){
        wherePh += s""" and c.place_id = ${placeId.get} """
      }
      val orderPh =
        """
          order by
            r.updatetime, r.reserve_id
        """
      SQL(selectPh + wherePh + orderPh).as(simple.*)
    }
  }


  /**
    * 予約情報の取得
    * @return
    */
  def selectReserveForPlot(placeId: Int, dateStr:String = ""): Seq[CarSummeryReservePlotInfo] = {

    val simple = {
        get[Int]("car_id") ~
        get[String]("car_no") ~
        get[Option[Int]]("floor_id") ~
        get[Option[Int]]("company_id")  map {
        case car_id ~ car_no  ~ floor_id  ~ company_id  =>
          CarSummeryReservePlotInfo(
              car_id.toString
            , car_no
            , floor_id.mkString
            , company_id.mkString
          )
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              c.car_id
            , c.car_no
            , r.floor_id
            , r.company_id
          from
            reserve_table r
            left join car_master c
              on r.car_id = c.car_id
          where
            c.active_flg = true
            and r.active_flg = true
            and c.place_id = {placeId}
            and r.reserve_date = to_date({dateStr}, 'YYYYMMDD')
          order by
            c.car_id
        """
      SQL(selectPh).on('placeId -> placeId, 'dateStr -> dateStr).as(simple.*)
    }
  }

}

