package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._

// JSON用 ----------------------------------
case class CarSummeryWorkPlotInfo(
    floorId: String
  , carId: String
  , carNo: String
  , companyId: String = ""
  , isWorking: Boolean
)

object CarSummeryWorkPlotInfo {
//
//  implicit val jsonReads: Reads[CarPlotInfo] = (
//      ((JsPath \ "floorId").read[String] | Reads.pure("")) ~
//      ((JsPath \ "carId").read[String] | Reads.pure("")) ~
//      ((JsPath \ "carNo").read[String] | Reads.pure("")) ~
//      ((JsPath \ "companyId").read[String] | Reads.pure("")) ~
//      ((JsPath \ "isWorking").read[Boolean] | Reads.pure(false))
//    )(CarPlotInfo.apply _)

  implicit def jsonWrites = Json.writes[CarSummeryWorkPlotInfo]
}

case class CarSummeryReservePlotInfo(
    floorId: String
  , carId: String
  , carNo: String
  , companyId: String = ""
)

object CarSummeryReservePlotInfo {
  //
  //  implicit val jsonReads: Reads[CarPlotInfo] = (
  //      ((JsPath \ "floorId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "carId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "carNo").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "companyId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "isWorking").read[Boolean] | Reads.pure(false))
  //    )(CarPlotInfo.apply _)

  implicit def jsonWrites = Json.writes[CarSummeryReservePlotInfo]
}

case class CarSummeryPlotInfo(
    workInfoList: List[CarSummeryWorkPlotInfo]
  , reserveInfoList: List[CarSummeryReservePlotInfo]
)

object CarSummeryPlotInfo {
  //
  //  implicit val jsonReads: Reads[CarPlotInfo] = (
  //      ((JsPath \ "floorId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "carId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "carNo").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "companyId").read[String] | Reads.pure("")) ~
  //      ((JsPath \ "isWorking").read[Boolean] | Reads.pure(false))
  //    )(CarPlotInfo.apply _)

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

case class ReserveInfo(
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
  def selectReserve(floorId:Option[Int] = None, companyId:Option[Int] = None, dateStr:String = ""): Seq[ReserveInfo] = {

    val simple = {
      get[Int]("reserve_id") ~
        get[Int]("car_id") ~
        get[Int]("car_btx_id") ~
        get[Int]("car_key_btx_id") ~
        get[Int]("floor_id") ~
        get[Int]("company_id") ~
        get[String]("reserve_date") map {
        case reserve_id ~ car_id ~ car_btx_id ~ car_key_btx_id ~ floor_id ~ company_id ~ reserve_date  =>
          ReserveInfo(reserve_id, car_id, car_btx_id, car_key_btx_id, floor_id, company_id, reserve_date)
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
      val orderPh =
        """
          order by
            r.reserve_id
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
          order by
            c.car_id
        """
      SQL(selectPh).on('placeId -> placeId).as(simple.*)
    }
  }

}

