package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.Logger
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
  def selectReserve(    placeId: Int
                      , floorIdList:Seq[Int] = Seq[Int]()
                      , dateStr:String = ""
                      , companyIdList:Seq[Int] = Seq[Int]()
                      , carIdList:Seq[Int] = Seq[Int]()
                   ): Seq[ReserveInfo] = {

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
      if(companyIdList.isEmpty == false){
        wherePh += s""" and r.company_id in ( ${companyIdList.mkString(",")} ) """
      }
      if(carIdList.isEmpty == false){
        wherePh += s""" and r.car_id in ( ${carIdList.mkString(",")} ) """
      }
      val orderPh =
        """
          order by
            r.floor_id
        """
      SQL(selectPh + wherePh + orderPh).as(simple.*)
    }
  }

  /**
    * 予約の削除
    * @return
    */
  def delete(reserveId:Int): Unit = {
    db.withTransaction { implicit connection =>
      // 削除
      SQL(
        """
          delete from reserve_table where reserve_id = {reserveId} ;
        """)
        .on('reserveId -> reserveId).executeUpdate()

      // コミット
      connection.commit()
      Logger.debug(s"""予約を削除、ID：${reserveId.toString}""")
    }
  }

  /**
    * 予約の更新
    * @return
    */
  def update(reserveId:Int, floorId:Int, companyId:Int, reserveDate:String): Unit = {
    db.withTransaction { implicit connection =>
      // 作業車マスタの更新
      SQL(
        """
          update reserve_table set
              floor_id = {floorId}
            , company_id = {companyId}
            , reserve_date = to_date({reserveDate}, 'YYYYMMDD')
            , updatetime = now()
          where reserve_id = {reserveId} ;
        """).on(
        'floorId -> floorId, 'companyId -> companyId, 'reserveDate -> reserveDate, 'reserveId -> reserveId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""予約を更新、ID：${reserveId.toString}""")
    }
  }

  /**
    * 予約の登録
    * @return
    */
  def insert(carId:Int, floorId:Int, companyId:Int, reserveDate:String): Int = {
    db.withTransaction { implicit connection =>
      // 作業車マスタの更新
      val id: Option[Long] = SQL(
        """
          insert into reserve_table (car_id, floor_id, company_id, reserve_date) values(
             {carId}, {floorId}, {companyId}, to_date({reserveDate}, 'YYYYMMDD')
          );
        """).on(
        'carId -> carId, 'floorId -> floorId, 'companyId -> companyId, 'reserveDate -> reserveDate
      ).executeInsert()

      // コミット
      connection.commit()

      Logger.debug(s"""予約を新規登録、ID：${id.get.toInt}""")
      id.get.toInt
    }
  }

}

