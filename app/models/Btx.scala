package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


case class BtxNameEnum(
    kindNameMap: Map[Int,String] = Map[Int,String](1 -> "作業車", 2 -> "作業車鍵", 3 -> "仮設材")
  , notePrefixMap: Map[Int,String] = Map[Int,String](1 -> "作業車番号：", 2 -> "作業車番号：", 3 -> "仮設材管理No.")
)

case class Btx(
  btxId: Int,
  placeId: Int
)

case class BtxTelemetryInfo(
  btxId: Int,
  powerLevel: Int = 0,
  kindName: String,
  name: String,
  note: String,
  isDetect: Boolean = true,
  isDbRegister: Boolean = true
)

@javax.inject.Singleton
class btxDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * BTXマスタ情報の取得
    * @return
    */
  def select(placeId:Int, btxIdList: Seq[Int] = Seq[Int]()): Seq[Btx] = {

    val simple = {
      get[Int]("btx_id") ~
        get[Int]("place_id")  map {
        case btx_id ~ place_id  =>
          Btx(btx_id, place_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              b.btx_id
            , b.place_id
          from
            place_master p
            inner join btx_master b
              on p.place_id = b.place_id
          where
            p.active_flg = true
            and b.active_flg = true
        """

      var wherePh = """ and p.place_id = {placeId} """
      if(btxIdList.isEmpty == false){
        wherePh += s""" and b.btx_id in (${btxIdList.mkString(",")}) """
      }
      val orderPh =
        """
          order by
            b.btx_id, b.place_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  /**
    * BTXマスタ情報の取得(テレメトリ画面用)
    * @return
    */
  def selectForTelemetry(placeId:Int): Seq[BtxTelemetryInfo] = {

    val simple = {
        get[Int]("btx_id") ~
        get[Int]("kind_code") ~
        get[String]("name") ~
        get[String]("note") map {
        case btx_id ~ kind_code ~ name ~ note  =>
          BtxTelemetryInfo(
              btxId = btx_id
            , powerLevel = 0
            , kindName = BtxNameEnum().kindNameMap(kind_code)
            , name = name
            , note = BtxNameEnum().notePrefixMap(kind_code) + note
          )
      }
    }

    db.withConnection { implicit connection =>

      val selectPh =
        """
          select
              tbl.btx_id
            , tbl.kind_code
            , tbl.name
            , tbl.note
          from
            (
              select
                  b.btx_id
                , 1 as kind_code
                , c.car_name as name
                , c.car_no as note
              from
                btx_master b
                inner join car_master c
                  on b.btx_id = c.car_btx_id
              where
                b.place_id = {placeId}
                and c.place_id = {placeId}
                and b.active_flg = true
                and c.active_flg = true
              union all
              select
                  b.btx_id
                , 2 as kind_code
                , c.car_name as name
                , c.car_no as note
              from
                btx_master b
                inner join car_master c
                  on b.btx_id = c.car_key_btx_id
              where
                b.place_id = {placeId}
                and c.place_id = {placeId}
                and b.active_flg = true
                and c.active_flg = true
              union all
              select
                  b.btx_id
                , 3 as kind_code
                , im.item_kind_name as name
                , it.item_no as note
              from
                btx_master b
                inner join item_table it
                  on b.btx_id = it.item_btx_id
                inner join item_kind_master im
                  on it.item_kind_id = im.item_kind_id
              where
                b.place_id = {placeId}
                and im.place_id = {placeId}
                and b.active_flg = true
                and it.active_flg = true
                and im.active_flg = true
            ) tbl
          order by tbl.btx_id
        """
      SQL(selectPh).on('placeId -> placeId).as(simple.*)
    }
  }

}

