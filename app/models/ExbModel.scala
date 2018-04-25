package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


case class ExbInfo(
  floorId: Int,
  exbDeviceNo: Int,
  exbDeviceId: Int
)

@javax.inject.Singleton
class exbModelDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * EXBマスタ情報の取得
    * @return
    */
  def selectExb(placeId: Int, floorId:Option[Int] = None): Seq[ExbInfo] = {

    val simple = {
      get[Int]("floor_id") ~
        get[Int]("exb_device_no") ~
        get[Int]("exb_device_id")  map {
        case floor_id ~ exb_device_no ~ exb_device_id  =>
          ExbInfo(floor_id, exb_device_no, exb_device_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              e.floor_id
            , e.exb_device_no
            , e.exb_device_id
          from
            place_master p
            inner join floor_master f
              on p.place_id = f.place_id
            inner join exb_master e
              on f.floor_id = e.floor_id
          where
            p.active_flg = true
            and f.active_flg = true
        """

      var wherePh = """ and p.place_id = {placeId} """
      if(floorId != None){
        wherePh += s""" and f.floor_id = ${floorId.get} """
      }
      val orderPh =
        """
          order by
            e.floor_id, e.exb_device_no
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

}

