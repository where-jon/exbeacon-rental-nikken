package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


case class ReserveInfo(
      reserveId: Int
    , carId: Int
    , floorId: Int
    , companyId: Int
    , reserveDateStr: String
)

@javax.inject.Singleton
class reserveDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 予約情報の取得
    * @return
    */
  def selectReserve(placeId: Int, floorIdList:Seq[Int] = Seq[Int](),dateStr:String = ""): Seq[ReserveInfo] = {

    val simple = {
        get[Int]("reserve_id") ~
        get[Int]("car_id") ~
        get[Int]("floor_id") ~
        get[Int]("company_id") ~
        get[String]("reserve_date") map {
        case reserve_id ~ car_id ~ floor_id ~ company_id ~ reserve_date  =>
          ReserveInfo(reserve_id, car_id, floor_id, company_id, reserve_date)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.reserve_id
            , r.car_id
            , r.floor_id
            , r.company_id
            , to_char(r.reserve_date, 'YYYYMMDD') as reserve_date
          from
            place_master p
            inner join floor_master f
              on p.place_id = f.place_id
            inner join reserve_table r
              on f.floor_id = r.floor_id
          where
            p.active_flg = true
            and f.active_flg = true
            and r.active_flg = true
        """

      var wherePh = ""
      wherePh += s""" and p.place_id = ${placeId} """

      if(floorIdList.isEmpty == false){
        wherePh += s""" and f.floor_id in ( ${floorIdList.mkString(",")} ) """
      }
      if(dateStr.isEmpty == false){
        wherePh += s""" and r.reserve_date = to_date('${dateStr}', 'YYYYMMDD') """
      }
      val orderPh =
        """
          order by
            r.floor_id
        """
      SQL(selectPh + wherePh + orderPh).as(simple.*)
    }
  }

}

