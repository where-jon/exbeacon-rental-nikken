package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


//case class ReserveInfo(
//      reserveId: Int
//    , carId: Int
//    , floorId: Int
//    , companyId: Int
//    , reserveDateStr: String
//)

@javax.inject.Singleton
class reserveDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

//  /**
//    * 予約情報の取得
//    * @return
//    */
//  def selectReserve(floorId:Option[Int] = None, companyId:Option[Int] = None, dateStr:String = ""): Seq[ReserveInfo] = {
//
//    val simple = {
//        get[Int]("reserve_id") ~
//        get[Int]("car_id") ~
//        get[Int]("floor_id") ~
//        get[Int]("company_id") ~
//        get[String]("reserve_date") map {
//        case reserve_id ~ car_id ~ floor_id ~ company_id ~ reserve_date  =>
//          ReserveInfo(reserve_id, car_id, floor_id, company_id, reserve_date)
//      }
//    }
//
//    db.withConnection { implicit connection =>
//      val selectPh =
//        """
//          select
//              reserve_id
//            , car_id
//            , floor_id
//            , company_id
//            , to_char(reserve_date, 'YYYYMMDD') as reserve_date
//          from
//            reserve_table
//          where
//            active_flg = true
//        """
//
//      var wherePh = ""
//      if(floorId != None){
//        wherePh += s""" and floor_id = ${floorId.get} """
//      }
//      if(companyId != None){
//        wherePh += s""" and company_id = ${companyId.get} """
//      }
//      if(dateStr.isEmpty == false){
//        wherePh += s""" and reserve_date = to_date('${dateStr}', 'YYYYMMDD') """
//      }
//      val orderPh =
//        """
//          order by
//            floor_id
//        """
//      SQL(selectPh + wherePh + orderPh).as(simple.*)
//    }
//  }

}

