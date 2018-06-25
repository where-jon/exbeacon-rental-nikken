package models

import java.sql.SQLException
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

case class Exb(
  exb_id: Int,
  exb_device_id: String,
  exb_name: String,
  exb_pos_name: String

)

case class ExbTotal(
  exb_id: Int,
  exb_device_id: String,
  exb_name: String,
  exb_pos_name: String,
  viewer_visible: Boolean,
  viewer_pos_type: String,
  viewer_pos_count: Int,
  viewer_pos_x: String,
  viewer_pos_y: String,
  viewer_pos_margin: Int,
  viewer_pos_floor: String,
  viewer_pos_size: Int

)

case class ExbData(
  exbId: List[Int],
  exbEditId: List[Int],
  exbDeviceId: List[String],
  exbName: List[String],
  exbPosName: List[String]

)

@javax.inject.Singleton
class ExbDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("exb_id") ~
      get[String]("exb_device_id") ~
      get[String]("exb_name") ~
      get[String]("exb_pos_name") map {
        case exb_id ~ exb_device_id ~ exb_name ~ exb_pos_name =>
          Exb(exb_id, exb_device_id, exb_name, exb_pos_name)
      }
  }

  // Parser
  val simpleTotal = {
    get[Int]("exb_id") ~
      get[String]("exb_device_id") ~
      get[String]("exb_name") ~
      get[String]("exb_pos_name") ~
      get[Boolean]("viewer_visible") ~
      get[String]("viewer_pos_type") ~
      get[Int]("viewer_pos_count") ~
      get[String]("viewer_pos_x") ~
      get[String]("viewer_pos_y") ~
      get[Int]("viewer_pos_margin") ~
      get[String]("viewer_pos_floor") ~
      get[Int]("viewer_pos_size") map {
        case exb_id ~ exb_device_id ~ exb_name ~ exb_pos_name ~ viewer_visible ~ viewer_pos_type ~ viewer_pos_count ~ viewer_pos_x ~ viewer_pos_y ~ viewer_pos_margin ~ viewer_pos_floor ~ viewer_pos_size =>
          ExbTotal(exb_id, exb_device_id, exb_name, exb_pos_name, viewer_visible, viewer_pos_type, viewer_pos_count, viewer_pos_x, viewer_pos_y, viewer_pos_margin, viewer_pos_floor, viewer_pos_size)
      }
  }

  def selectAll(): Seq[Exb] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
        select exb_id, exb_device_id, exb_name, exb_pos_name
        from exb_viewer order by exb_id;
        """)

      sql.as(simple.*)
    }
  }

  def selectTotal(): Seq[ExbTotal] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
        select
        exb_id,
        exb_device_id,
        exb_name,
        exb_pos_name,
        coalesce(viewer_visible, 'FALSE') as viewer_visible,
        coalesce(viewer_pos_type, 'none') as viewer_pos_type,
        coalesce(viewer_pos_count, 1) as viewer_pos_count,
        coalesce(viewer_pos_x, '0') as viewer_pos_x,
        coalesce(viewer_pos_y, '0') as viewer_pos_y,
        coalesce(viewer_pos_margin, -1) as viewer_pos_margin,
        coalesce(viewer_pos_floor, '3') as viewer_pos_floor,
        coalesce(viewer_pos_size, '35') as viewer_pos_size

        from exb_viewer order by exb_id;
        """)

      sql.as(simpleTotal.*)
    }
  }

  def deleteExb(exb: ExbData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      val statement = connection.createStatement()
      var num = 0
      System.out.println("exb.exbId.length" + exb.exbId.length)
      var vEndPoint = exb.exbId.length - 1;
      for (num <- 0 to vEndPoint) {
        var exb_id = exb.exbId(num);
        val sql = SQL("""
              delete from exb_viewer
                  where exb_id = {exb_id};
              """).on(
          'exb_id -> exb_id
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

  def updateExb(exb: ExbData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>

      val statement = connection.createStatement()
      var num = 0
      System.out.println("exb.exbId.length" + exb.exbId.length)
      var vEndPoint = exb.exbId.length - 1;

      for (num <- 0 to vEndPoint) {
        var exb_id = exb.exbId(num);
        var exb_edit_id = exb.exbEditId(num);
        var exb_device_id = exb.exbDeviceId(num);
        var exb_name = exb.exbName(num);
        var exb_pos_name = exb.exbPosName(num);

        System.out.println("exb_id" + exb_id)
        System.out.println("exb_edit_id" + exb_edit_id)
        System.out.println("exb_device_id" + exb_device_id)
        System.out.println("exb_name" + exb_name)
        System.out.println("exb_pos_name" + exb_pos_name)
        //var vNum = num + 1;
        val sql = SQL("""
              update exb_viewer
                  set exb_pos_name = {exb_pos_name},
                  	  exb_device_id = {exb_device_id},
        			  exb_id = {exb_edit_id},
                  	  exb_name = {exb_name} 
                  where exb_id = {exb_id};
                  
               INSERT INTO exb_viewer (exb_id,exb_device_id,exb_name,exb_pos_name)
               select {exb_edit_id}, {exb_device_id}, {exb_name}, {exb_pos_name}
               
               where not exists (select 1 from exb_viewer where exb_id = {exb_edit_id});
               
              """).on(
          'exb_id -> exb_id,
          'exb_edit_id -> exb_edit_id,
          'exb_device_id -> exb_device_id,
          'exb_name -> exb_name,
          'exb_pos_name -> exb_pos_name
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
  def addRecordExbTotal(exb: ExbTotal): Int = {
    db.withConnection { implicit conntcition =>
      val sql = SQL("""
        insert into exb_viewer (exb_id,exb_device_id,exb_name, exb_pos_name,
          viewer_visible, viewer_pos_type, viewer_pos_count, viewer_pos_x, viewer_pos_y, viewer_pos_margin, viewer_pos_floor, viewer_pos_size
        ) values (
        {exb_id},{exb_device_id}, {exb_name}, {exb_pos_name}
        , {viewer_visible}, {viewer_pos_type}, {viewer_pos_count}, {viewer_pos_x}, {viewer_pos_y}, {viewer_pos_margin}, {viewer_pos_floor}, {viewer_pos_size}
        );
        """).on(
        'exb_id -> exb.exb_id,
        'exb_device_id -> exb.exb_device_id,
        'exb_name -> exb.exb_name,
        'exb_pos_name -> exb.exb_pos_name,

        'viewer_visible -> exb.viewer_visible,
        'viewer_pos_type -> exb.viewer_pos_type,
        'viewer_pos_count -> exb.viewer_pos_count,
        'viewer_pos_x -> exb.viewer_pos_x,
        'viewer_pos_y -> exb.viewer_pos_y,
        'viewer_pos_margin -> exb.viewer_pos_margin,
        'viewer_pos_floor -> exb.viewer_pos_floor,
        'viewer_pos_size -> exb.viewer_pos_size

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