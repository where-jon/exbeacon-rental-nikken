package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
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

@javax.inject.Singleton
class floorDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

//  val simple = {
//      get[Int]("room_id") ~
//      get[String]("room_name") ~
//      get[String]("description") map {
//      case room_id ~ room_name ~ description  =>
//        Room(room_id, room_name, description)
//    }
//  }
//
//  /**
//    * 会議室情報を部屋番号順で全て取得
//    * @return
//    */
//  def selectRooms(mapId: String = ""): Seq[Room] = {
//    db.withConnection { implicit connection =>
//
//      var selectPhrase = "select room_id, room_name, description from conference_room_master where delete_flag = 0 "
//      val orderPhrase = "order by room_id"
//
//      if(mapId != ""){
//        selectPhrase += "and map_id = " + mapId
//      }
//
//      SQL(selectPhrase + orderPhrase).as(simple.*)
//    }
//  }
//
//  /**
//    * フロアのIDと名前の取得
//    * @return
//    */
//  def selectFloor(): Seq[MapViewer] = {
//
//    val parser = {
//      get[Int]("map_id") ~
//        get[String]("map_name")  map {
//        case map_id ~ map_name =>
//          MapViewer(map_id, 0, 0, "", map_name)
//      }
//    }
//
//    db.withConnection { implicit connection =>
//      val sql = SQL("""
//        select distinct
//            m.map_id
//          , m.map_name
//        from
//          conference_room_master r
//          inner join map_master m
//            on r.map_id = m.map_id
//        order by map_id desc;
//        """)
//
//      sql.as(parser.*)
//    }
//  }

}

