package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._


case class WorkType(
                     work_type_id :Int,
                     work_type_name :String,
                     note  :String,
                     place_id :Int,
                     active_flg :Boolean
                   )

@javax.inject.Singleton
class WorkTypeDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("work_type_id") ~
      get[String]("work_type_name") ~
      get[String]("note") ~
      get[Int]("place_id") ~
      get[Boolean]("active_flg") map {
      case work_type_id ~ work_type_name ~ note ~ place_id ~ active_flg=>
        WorkType(work_type_id ,work_type_name ,note ,place_id ,active_flg)
    }
  }

  def selectAll(): Seq[WorkType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select work_type_id ,work_type_name ,note ,place_id ,active_flg
              from work_type order by work_type_id;
              """)
      sql.as(simple.*)
    }
  }

  def selectWorkInfo(placeId: Int): Seq[WorkType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select work_type_id ,work_type_name ,note ,place_id ,active_flg
              from work_type
              where active_flg = true
              order by work_type_id;
              """)
      sql.as(simple.*)
    }
  }
}