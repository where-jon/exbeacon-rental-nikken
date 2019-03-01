package models.system

import java.sql.SQLException
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

case class MapViewer(
  map_id: Int,
  map_width: Int,
  map_height: Int,
  map_image: String,
  map_position: String
)

case class MapViewerData(
  mapId: List[Int],
  mapWidth: List[Int],
  mapHeight: List[Int],
  mapImage: List[String],
  mapPostion: List[String]
)

@javax.inject.Singleton
class FloorMapDAO @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("map_id") ~
      get[Int]("map_width") ~
      get[Int]("map_height") ~
      get[String]("map_image") ~
      get[String]("map_position") map {
        case map_id ~ map_width ~ map_height ~ map_image ~ map_position =>
          MapViewer(map_id, map_width, map_height, map_image, map_position)
      }
  }

  def selectAll(): Seq[MapViewer] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
        select map_id, map_width, map_height, map_image, map_position
        from map_viewer order by map_id desc;
        """)

      sql.as(simple.*)
    }
  }

  def deleteMapViewer(mapViewer: MapViewerData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      val statement = connection.createStatement()
      var num = 0
      System.out.println("length" + mapViewer.mapId.length)
      var vEndPoint = mapViewer.mapId.length - 1;
      for (num <- 0 to vEndPoint) {
        var map_id = mapViewer.mapId(num);
        val sql = SQL("""
              delete from map_viewer
                  where map_id = {map_id};
              """).on(
          'map_id -> map_id
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error" + e)
            if (!vCheck) {
              vCheck = true;
              vResult = e + ""
            }
          }
        }
      }
    }
    vResult
  }

  def updateMapViewer(mapViewer: MapViewerData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>

      val statement = connection.createStatement()
      var num = 0
      System.out.println("length" + mapViewer.mapId.length)
      var vEndPoint = mapViewer.mapId.length - 1;

      for (num <- 0 to vEndPoint) {
        var map_id = mapViewer.mapId(num);
        var map_width = mapViewer.mapWidth(num);
        var map_height = mapViewer.mapHeight(num);
        var map_image = mapViewer.mapImage(num);
        var map_position = mapViewer.mapPostion(num);

        //var vNum = num + 1;
        val sql = SQL("""
              update map_viewer
                  set map_width = {map_width},
                  	  map_height = {map_height},
        			        map_image = {map_image},
                      map_position = {map_position}
                  where map_id = {map_id};
                  
               INSERT INTO map_viewer (map_id,map_width,map_height,map_image,map_position)
               select {map_id}, {map_width}, {map_height}, {map_image}, {map_position}
               
               where not exists (select 1 from map_viewer where map_id = {map_id});
               
              """).on(
          'map_id -> map_id,
          'map_width -> map_width,
          'map_height -> map_height,
          'map_image -> map_image,
          'map_position -> map_position
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error " + e)
            if (!vCheck) {
              vCheck = true;
              vResult = e + ""
            }
            // breakPoint.break
          }
        }
      }

    }
    vResult
  }

  // インポート用
  def addRecordMapViewer(mapViewer: MapViewer): Int = {
    db.withConnection { implicit conntcition =>

      val sql = SQL("""
        insert into map_viewer (map_id,map_width,map_height,map_image,map_position
        ) values (
        {map_id}, {map_width}, {map_height}, {map_image}, {map_position}
        );
        """).on(
        'map_id -> mapViewer.map_id,
        'map_width -> mapViewer.map_width,
        'map_height -> mapViewer.map_height,
        'map_image -> mapViewer.map_image,
        'map_position -> mapViewer.map_position
      )
      sql.executeUpdate()
    }
  }

  def deleteAll(): Int = {
    db.withConnection { implicit connection =>
      val sql = SQL("delete from map_viewer")

      sql.executeUpdate()
    }
  }

  def updateMapData(map_id: Int, map_image: String, map_width: Int, map_height: Int): Int = {
    //System.out.println("map_id::" + map_id);
    //System.out.println("map_image::" + map_image);
    System.out.println("=======data===========");
    db.withConnection { implicit connection =>
      val sql = SQL("""
        update map_viewer
            set map_image = {map_image},
            map_width = {map_width},
            map_height = {map_height}
            where map_id = {map_id};
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