package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

//case class roomPosition(
//  room_id: String,
//  room_name: String,
//  description: String
//)
//object roomPosition {
//
//  implicit val jsonReads: Reads[roomPosition] = (
//      ((JsPath \ "room_id").read[String] | Reads.pure("")) ~
//      ((JsPath \ "room_name").read[String] | Reads.pure(""))~
//      ((JsPath \ "description").read[String] | Reads.pure(""))
//    )(roomPosition.apply _)
//
//  implicit def jsonWrites = Json.writes[roomPosition]
//}

case class FloorSummery(
  floor: String,
  cntMorning: String,
  cntReserve: String
)

case class FloorInfo(
  floorId: Int,
  floorName: String,
  exbDeviceIdList: Seq[String]
)

@javax.inject.Singleton
class floorDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * フロア情報の取得
    * @return
    */
  def selectFloorInfo(placeId: Int, floorName: String = ""): Seq[FloorInfo] = {

    val simple = {
      get[Int]("floor_id") ~
        get[String]("floor_name") ~
        get[String]("exb_device_id_str") map {
        case floor_id ~ floor_name ~ exb_device_id_str  =>
          FloorInfo(floor_id, floor_name, exb_device_id_str.split(",").toSeq)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              f.floor_id
            , f.floor_name
            , ARRAY_TO_STRING(
                ARRAY(
                  SELECT
                    e.exb_device_id
                  FROM
                    exb_master e
                  WHERE
                    e.floor_id = f.floor_id
                  ORDER BY
                    e.exb_device_id
                )
              , ',') as exb_device_id_str
          from
            place_master p
            inner join floor_master f
              on p.place_id = f.place_id
              and p.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(floorName.isEmpty == false){
        wherePh += s""" and f.floor_name = '${floorName}' """
      }

      val orderPh =
        """
          order by
            f.display_order
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }


  /**
    * フロアの削除
    * @return
    */
  def deleteById(floorId:Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL("""delete from floor_master where floor_id = {floorId} ;""").on('floorId -> floorId).executeUpdate()
      SQL("""delete from exb_master where floor_id = {floorId} ;""").on('floorId -> floorId).executeUpdate()
      // コミット
      connection.commit()

      Logger.debug(s"""フロアを削除、ID：" + ${floorId.toString}""")
    }
  }

  /**
    * フロアの新規登録
    * @return
    */
  def insert(floorName:String, placeId: Int, deviceIdList: Seq[String]) = {

    db.withTransaction { implicit connection =>
      // 順序の取得
      val selectQuery = "select coalesce(max(display_order), 0) from floor_master where place_id = " + placeId
      var cnt = SQL(selectQuery).as(scalar[Int].single)

      cnt = cnt + 1

      // フロアマスタへの登録
      val params: Seq[NamedParameter] = Seq(
        "floorName" -> floorName,
        "displayOrder" -> cnt,
        "placeId" -> placeId
      )
      var insertSql = SQL(
        """
          insert into floor_master (floor_name, display_order, place_id)
          values ({floorName}, {displayOrder}, {placeId})
        """
      ).on(params:_*)

      // SQL実行
      val floorId: Option[Long] = insertSql.executeInsert()
Logger.debug(s"""フロアを登録、ID：" + ${floorId.get.toInt}""")

      // exbマスタへの登録
      val indexedValues = deviceIdList.zipWithIndex

      val rows = indexedValues.map{ case (value, i) =>
          s"""({exb_device_id_${i}}, {floor_id_${i}})"""
      }.mkString(",")

      val parameters = indexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"exb_device_id_${i}" , value),
          NamedParameter(s"floor_id_${i}" , floorId.get.toInt)
        )
      }

      // SQL実行
      BatchSql(s""" insert into exb_master (exb_device_id, floor_id) values ${rows} """, parameters).execute

      // コミット
      connection.commit()
Logger.debug(s"""EXBマスタを登録""")
    }
  }


  /**
    * フロアの新規登録
    * @return
    */
  def updateById(floorId: Int, floorName: String, deviceIdList: Seq[String]) = {

    db.withTransaction { implicit connection =>
      // フロアの更新
      val params: Seq[NamedParameter] = Seq(
        "floorName" -> floorName,
        "floorId" -> floorId)
      SQL(
        """
          update floor_master
          set floor_name = {floorName}, updatetime = now()
          where floor_id = {floorId};
        """
      ).on(params:_*).executeUpdate()
Logger.debug(s"""フロアを更新、ID：" + ${floorId}""")
      // EXBマスタの削除
      SQL(
        """
          delete from exb_master
          where floor_id = {floorId}
        """
      ).on('floorId -> floorId).executeUpdate()

      // EXBマスタの登録
      val indexedValues = deviceIdList.zipWithIndex

      val rows = indexedValues.map{ case (value, i) =>
        s"""({exb_device_id_${i}}, {floor_id_${i}})"""
      }.mkString(",")

      val parameters = indexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"exb_device_id_${i}" , value),
          NamedParameter(s"floor_id_${i}" , floorId)
        )
      }

      // SQL実行
      BatchSql(s""" insert into exb_master (exb_device_id, floor_id) values ${rows} """, parameters).execute

      // コミット
      connection.commit()
Logger.debug(s"""EXBマスタを更新""")
    }
  }

}

