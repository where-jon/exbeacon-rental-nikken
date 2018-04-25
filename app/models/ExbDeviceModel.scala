package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


case class ExbDeviceInfo(
  place_id: Int,
  exbDeviceNo: Int,
  exbDeviceId: Int
)

@javax.inject.Singleton
class exbDeviceModelDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * EXBデバイスマスタ情報の取得
    * @return
    */
  def select(placeId: Int, exbDeviceNo:Option[Int] = None): Seq[ExbDeviceInfo] = {

    val simple = {
      get[Int]("place_id") ~
        get[Int]("exb_device_no") ~
        get[Int]("exb_device_id")  map {
        case place_id ~ exb_device_no ~ exb_device_id  =>
          ExbDeviceInfo(place_id, exb_device_no, exb_device_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              e.place_id
            , e.exb_device_no
            , e.exb_device_id
          from
            exb_device_master e
          where
            1 = 1
        """

      var wherePh = """ and e.place_id = {placeId} """
      if(exbDeviceNo != None){
        wherePh += s""" and e.exb_device_no = ${exbDeviceNo.get} """
      }
      val orderPh =
        """
          order by
            e.exb_device_no
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

}

