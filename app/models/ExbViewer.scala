package models

import java.sql.SQLException
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

case class ExbViewer(
  viewer_id: Int,
  viewer_visible: Boolean,
  viewer_pos_type: String,
  viewer_pos_x: String,
  viewer_pos_y: String,
  viewer_pos_margin: Int,
  viewer_pos_count: Int,
  viewer_pos_floor: String,
  viewer_pos_num: Int

)

case class ExbViewerTotal(
  exb_id: Int,
  viewer_visible: Boolean,
  viewer_pos_type: String,
  viewer_pos_x: String,
  viewer_pos_y: String,
  viewer_pos_margin: Int,
  viewer_pos_count: Int,
  viewer_pos_floor: String,
  viewer_pos_size: Int,
  exb_name: String,
  exb_pos_name: String,
  exb_device_id: String,
  display_limit: Int

)

case class ExbViewerData(
  viewerId: List[Int],
  viewerVisible: List[Boolean],
  viewerPosType: List[String],
  viewerPosX: List[String],
  viewerPosY: List[String],
  viewerPosMargin: List[Int],
  viewerPosCount: List[Int],
  viewerPosFloor: List[String],
  viewerPosSize: List[Int],
  viewerPosNum: List[Int]
)

@javax.inject.Singleton
class ExbViewerDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("exb_id") ~
      get[Boolean]("viewer_visible") ~
      get[String]("viewer_pos_type") ~
      get[String]("viewer_pos_x") ~
      get[String]("viewer_pos_y") ~
      get[Int]("viewer_pos_margin") ~
      get[Int]("viewer_pos_count") ~
      get[String]("viewer_pos_floor") ~
      get[Int]("viewer_pos_size") ~
      get[String]("exb_name") ~
      get[String]("exb_pos_name") ~
      get[String]("exb_device_id") ~
      get[Int]("display_limit") map {
        case exb_id ~ viewer_visible ~ viewer_pos_type ~ viewer_pos_x ~ viewer_pos_y ~ viewer_pos_margin ~ viewer_pos_count ~ viewer_pos_floor ~ viewer_pos_size ~ exb_name ~ exb_pos_name ~ exb_device_id ~ display_limit =>
          ExbViewerTotal(exb_id, viewer_visible, viewer_pos_type, viewer_pos_x, viewer_pos_y, viewer_pos_margin, viewer_pos_count, viewer_pos_floor, viewer_pos_size, exb_name, exb_pos_name, exb_device_id, display_limit)
      }
  }

  def selectAll(): Seq[ExbViewerTotal] = {
    db.withConnection { implicit connection =>

      val sql = SQL("""
          select
          exb_id
          ,coalesce(viewer_pos_type, 'none') as viewer_pos_type
          ,coalesce(viewer_visible, 'FALSE') as viewer_visible
          ,coalesce(viewer_pos_count, 1) as viewer_pos_count
          ,coalesce(viewer_pos_x, '0') as viewer_pos_x
          ,coalesce(viewer_pos_y, '0') as viewer_pos_y
          ,coalesce(viewer_pos_margin, -1) as viewer_pos_margin
          ,coalesce(viewer_pos_floor, '3') as viewer_pos_floor
          ,coalesce(viewer_pos_size, '35') as viewer_pos_size
          ,exb_name
          ,exb_pos_name
          ,exb_device_id
          ,display_limit from exb_viewer order by exb_id;


        """)

      sql.as(simple.*)
    }
  }

  def deleteExbViewer(exbViewer: ExbViewerData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      val statement = connection.createStatement()
      var num = 0
      System.out.println("length" + exbViewer.viewerId.length)
      var vEndPoint = exbViewer.viewerId.length - 1;
      for (num <- 0 to vEndPoint) {
        var viewer_id = exbViewer.viewerId(num);
        val sql = SQL("""
              delete from exb_viewer
                  where viewer_id = {viewer_id};
              """).on(
          'viewer_id -> viewer_id
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

  def updateExbViewer(exbViewer: ExbViewerData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>

      val statement = connection.createStatement()
      var num = 0
      System.out.println("length" + exbViewer.viewerId.length)
      var vEndPoint = exbViewer.viewerId.length - 1;

      for (num <- 0 to vEndPoint) {
        var viewer_id = exbViewer.viewerId(num);
        var viewer_visible = exbViewer.viewerVisible(num);
        var viewer_pos_type = exbViewer.viewerPosType(num);
        var viewer_pos_x = exbViewer.viewerPosX(num);
        var viewer_pos_y = exbViewer.viewerPosY(num);
        var viewer_pos_margin = exbViewer.viewerPosMargin(num);
        var viewer_pos_count = exbViewer.viewerPosCount(num);
        var viewer_pos_floor = exbViewer.viewerPosFloor(num);
        var viewer_pos_size = exbViewer.viewerPosSize(num);
        var viewer_pos_num = exbViewer.viewerPosNum(num);

        //var vNum = num + 1;
        val sql = SQL("""
              update exb_viewer
                  set viewer_visible = {viewer_visible},
                  	  viewer_pos_type = {viewer_pos_type},
        			        viewer_pos_x = {viewer_pos_x},
                      viewer_pos_y = {viewer_pos_y},
                      viewer_pos_margin = {viewer_pos_margin},
                      viewer_pos_count = {viewer_pos_count},
                      viewer_pos_floor = {viewer_pos_floor},
                      viewer_pos_size = {viewer_pos_size}
                  where exb_id = {viewer_id};
                  
               INSERT INTO exb_viewer (exb_id,viewer_visible,viewer_pos_type,viewer_pos_x,viewer_pos_y,viewer_pos_margin,viewer_pos_count,viewer_pos_floor,viewer_pos_size)
               select {viewer_id}, {viewer_visible}, {viewer_pos_type}, {viewer_pos_x}, {viewer_pos_y}, {viewer_pos_margin},{viewer_pos_count},{viewer_pos_floor},{viewer_pos_size}
               
               where not exists (select 1 from exb_viewer where exb_id = {viewer_id});
               
              """).on(
          'viewer_id -> viewer_id,
          'viewer_visible -> viewer_visible,
          'viewer_pos_type -> viewer_pos_type,
          'viewer_pos_x -> viewer_pos_x,
          'viewer_pos_y -> viewer_pos_y,
          'viewer_pos_margin -> viewer_pos_margin,
          'viewer_pos_count -> viewer_pos_count,
          'viewer_pos_floor -> viewer_pos_floor,
          'viewer_pos_size -> viewer_pos_size
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
  def addRecordExbViewer(exbViewer: ExbViewer): Int = {
    db.withConnection { implicit conntcition =>
      val sql = SQL("""
        insert into exb_viewer (viewer_id,viewer_visible,viewer_pos_type,viewer_pos_x,viewer_pos_y,
         viewer_pos_margin,viewer_pos_count,viewer_pos_floor,viewer_pos_num
        ) values (
        {viewer_id}, {viewer_visible}, {viewer_pos_type}, {viewer_pos_x}, {viewer_pos_y}, {viewer_pos_margin}, {viewer_pos_count},{viewer_pos_floor},{viewer_pos_num}
        );
        """).on(
        'viewer_id -> exbViewer.viewer_id,
        'viewer_visible -> exbViewer.viewer_visible,
        'viewer_pos_type -> exbViewer.viewer_pos_type,
        'viewer_pos_x -> exbViewer.viewer_pos_x,
        'viewer_pos_y -> exbViewer.viewer_pos_y,
        'viewer_pos_margin -> exbViewer.viewer_pos_margin,
        'viewer_pos_count -> exbViewer.viewer_pos_count,
        'viewer_pos_floor -> exbViewer.viewer_pos_floor,
        'viewer_pos_num -> exbViewer.viewer_pos_num
      )
      sql.executeUpdate()
    }
  }

  def deleteAll(): Int = {
    db.withConnection { implicit connection =>
      val sql = SQL("delete from exb_viewer;")

      sql.executeUpdate()
    }
  }
}