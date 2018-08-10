package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

case class FloorSortPostJsonRequestObj(
   floorIdComma: String
  ,placeId: Int = 0
)
object FloorSortPostJsonRequestObj {

  implicit val jsonReads: Reads[FloorSortPostJsonRequestObj] = (
    ((JsPath \ "floorIdComma").read[String] | Reads.pure("")) ~
      ((JsPath \ "placeId").read[Int] | Reads.pure(0))
    )(FloorSortPostJsonRequestObj.apply _)

  implicit def jsonWrites = Json.writes[FloorSortPostJsonRequestObj]
}


case class FloorAll(
  floor_id: Int,
  floor_name: String,
  display_order: Int,
  place_id: Int,
  active_flg: Boolean,
  floor_map_width: Int,
  floor_map_height: Int,
  floor_map_image: String
)

case class FloorSummery(
  floor: String,
  cntMorning: String,
  cntReserve: String
)

case class Floor(
  floor_Id: Int,
  floor_name: String
)

case class FloorMapInfo(
  floorId: Int,
  floorName: String,
  exbDeviceNoList: Seq[String],
  exbDeviceIdList: Seq[String]
)

case class FloorInfo(
  floorId: Int,
  floorName: String,
  exbDeviceNoList: Seq[String],
  exbDeviceIdList: Seq[String]
)


case class FloorInfoData(
  floor_id: Int,
  display_order: Int,
  active_flg:Boolean,
  floor_name: String,
  exbDeviceNoList: Seq[String],
  exbDeviceIdList: Seq[String]
)

@javax.inject.Singleton
class floorDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * フロア情報だけ取得
    * @return
    */
  def selectFloor(placeId: Int): Seq[Floor] = {

    val simple = {
      get[Int]("floor_id") ~
        get[String]("floor_name")map {
        case floor_id ~ floor_name  =>
          Floor(floor_id, floor_name)
      }
    }

    db.withConnection { implicit connection =>
      val sql = SQL("""
              select floor_id ,floor_name
              from floor_master
              where place_id = {placeId}
              and active_flg = true
              order by floor_id;
              """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }

  /**
    * フロア情報の取得
    * @return
    */
  def selectFloorInfo(placeId: Int, floorName: String = "", floorId: Option[Int] = None): Seq[FloorInfo] = {

    val simple = {
      get[Int]("floor_id") ~
        get[String]("floor_name") ~
        get[String]("exb_device_no_str") ~
        get[String]("exb_device_id_str")  map {
        case floor_id ~ floor_name ~ exb_device_no_str ~ exb_device_id_str  =>
          FloorInfo(floor_id, floor_name, exb_device_no_str.split(",").toSeq, exb_device_id_str.split(",").toSeq)
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
                    e.exb_device_no
                  FROM
                    exb_master e
                  WHERE
                    e.floor_id = f.floor_id
                    and e.place_id = {placeId}
                  ORDER BY
                    e.exb_device_no
                )
              , ',') as exb_device_no_str
            , ARRAY_TO_STRING(
                ARRAY(
                  SELECT
                    e.exb_device_id
                  FROM
                    exb_master e
                  WHERE
                    e.floor_id = f.floor_id
                    and e.place_id = {placeId}
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
      if(floorId != None){
        wherePh += s""" and f.floor_id = ${floorId.get} """
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
  def insert(floorName:String,displayOrder:Int,activeFlg:Boolean, placeId: Int) = {

    db.withTransaction { implicit connection =>
      // 順序の取得
//      val selectQuery = s"""select coalesce(max(display_order), 0) from floor_master where place_id = ${placeId}"""
//      var cnt = SQL(selectQuery).as(scalar[Int].single)
//      cnt = cnt + 1
      // フロアマスタへの登録
      val params: Seq[NamedParameter] = Seq(
        "floorName" -> floorName,
        "displayOrder" -> displayOrder,
        "activeFlg" -> activeFlg,
        "placeId" -> placeId
      )
      val insertSql = SQL(
        """
           insert into floor_master (floor_name, display_order, place_id,active_flg,updatetime,floor_map_width,floor_map_height,floor_map_image)
           values ({floorName}, {displayOrder}, {placeId},{activeFlg},now(),0,0,'')

        """
      ).on(params:_*)

      // SQL実行
      val floorId: Option[Long] = insertSql.executeInsert()
      Logger.debug(s"""フロアを登録、ID：" + ${floorId.get.toInt}""")
    }
  }


  /**
    * フロアの更新
    * @return
    */
  def update(floorId:Int,floorName:String,displayOrder:Int,activeFlg:Boolean, placeId: Int) = {

    db.withTransaction { implicit connection =>
      SQL(
        """
          update floor_master set
              floor_name = {floorName}
            , display_order = {displayOrder}
            , active_flg = {activeFlg}
            , updatetime = now()
          where floor_id = {floorId}
          and place_id = {placeId};
        """).on(
        'floorName -> floorName,
              'displayOrder -> displayOrder,
              'activeFlg -> activeFlg,
              'floorId -> floorId,
              'placeId -> placeId
      ).executeUpdate()
      // コミット
      connection.commit()
      Logger.debug(s"""フロアを更新、ID：" + ${floorId.toString}""")
    }
  }


  /**
    * フロアの新規登録
    * @return
    */
  def updateById(placeId: Int, floorId: Int, floorName: String, deviceList: Seq[(Int,Int)]) = {

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
      val indexedValues = deviceList.zipWithIndex

      val rows = indexedValues.map{ case (value, i) =>
        s"""({place_id_${i}}, {exb_device_no_${i}}, {exb_device_id_${i}}, {floor_id_${i}})"""
      }.mkString(",")

      val parameters = indexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"place_id_${i}" , placeId),
          NamedParameter(s"exb_device_no_${i}" , value._1),
          NamedParameter(s"exb_device_id_${i}" , value._2),
          NamedParameter(s"floor_id_${i}" , floorId)
        )
      }

      // SQL実行
      BatchSql(s""" insert into exb_master (place_id, exb_device_no, exb_device_id, floor_id) values ${rows} """, parameters).execute

      // コミット
      connection.commit()
Logger.debug(s"""EXBマスタを更新""")
    }
  }

  /**
    * フロアの表示順の更新
    * @return
    */
  def updateOrder(floorIdList: Seq[Int]) = {
    db.withTransaction { implicit connection =>

      floorIdList.zipWithIndex.foreach{ case (floorId:Int, i:Int) =>
        SQL("update floor_master set display_order = {i}, updatetime = now() where floor_id = {floorId}")
            .on('i -> i, 'floorId -> floorId)
              .executeUpdate()
      }

      // コミット
      connection.commit()
    }
  }


  /*exbeacn情報を取得 Parser 20180724*/
  val simpleFloorAll = {
      get[Int]("floor_id") ~
      get[String]("floor_name") ~
      get[Int]("display_order") ~
      get[Int]("place_id") ~
      get[Boolean]("active_flg") ~
      get[Int]("floor_map_width") ~
      get[Int]("floor_map_height") ~
      get[String]("floor_map_image")map {
        case floor_id ~ floor_name ~ display_order ~ place_id ~ active_flg ~ floor_map_width ~ floor_map_height ~ floor_map_image =>
          FloorAll(floor_id, floor_name, display_order, place_id, active_flg,floor_map_width, floor_map_height, floor_map_image)
      }
  }

  /**
    * フロア情報の取得
    * @return
    */
  def selectFloorAll(placeId: Int): Seq[FloorAll] = {

    db.withConnection { implicit connection =>
      val sql = SQL("""
        select
          floor_id,
          floor_name ,
          display_order,
          place_id ,
          active_flg,
          floor_map_width,
          floor_map_height,
          floor_map_image
          from floor_master as floor
          where floor.place_id = {placeId}
          --and floor.active_flg = true
          order by display_order;
       """).on(
        "placeId" -> placeId
      )

      sql.as(simpleFloorAll.*)
    }
  }

  /**
    * フロア管理画面表示データ取得
    * @return
    */
  //def selectFloorAll(placeId: Int) = {
  def selectFloorInfoData(placeId: Int): Seq[FloorInfoData] = {

    val simple = {
      get[Int]("floor_id") ~
        get[Int]("display_order") ~
        get[Boolean]("active_flg") ~
        get[String]("floor_name") ~
        get[String]("exb_device_no_str") ~
        get[String]("exb_device_id_str")  map {
        case floor_id ~ display_order ~active_flg~floor_name ~ exb_device_no_str ~ exb_device_id_str  =>
          FloorInfoData(floor_id, display_order,active_flg,floor_name, exb_device_no_str.split(",").toSeq, exb_device_id_str.split(",").toSeq)
      }
    }

    db.withConnection { implicit connection =>
      val sql = SQL("""
        select
             f.floor_id
             , f.display_order
             , f.floor_name
             , f.active_flg
             , ARRAY_TO_STRING(
               ARRAY(
                 SELECT
                   e.exb_device_no
                 FROM
                 exb_master e
                 WHERE
                 e.floor_id = f.floor_id
             and e.place_id = {placeId}
             ORDER BY
               e.exb_device_no
             )
             , ',') as exb_device_no_str
             , ARRAY_TO_STRING(
               ARRAY(
                 SELECT
                   e.exb_device_id
                 FROM
                 exb_master e
                 WHERE
                 e.floor_id = f.floor_id
             and e.place_id = {placeId}
             ORDER BY
               e.exb_device_id
             )
             , ',') as exb_device_id_str
               from
             place_master p
               inner join floor_master f
               on p.place_id = f.place_id
             and p.active_flg = true
             where f.place_id = {placeId}
             order by f.display_order

       """).on(
        "placeId" -> placeId
      )

      sql.as(simple.*)
    }
  }



  def updateFloorMap(map_id: Int, map_image: String, map_width: Int, map_height: Int): Int = {
    //System.out.println("map_id::" + map_id);
    //System.out.println("map_image::" + map_image);
    System.out.println("=======data===========");
    db.withConnection { implicit connection =>
      val sql = SQL("""
        update floor_master
            set floor_map_image = {map_image},
            floor_map_width = {map_width},
            floor_map_height = {map_height}
            where floor_id = {map_id};
        """).on(
        "map_id" -> map_id,
        "map_image" -> map_image,
        "map_width" -> map_width,
        "map_height" -> map_height
      )

      sql.executeUpdate()
    }
  }

}

