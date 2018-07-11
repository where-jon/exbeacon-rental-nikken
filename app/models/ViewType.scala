package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

case class ViewType(
  view_type_id: Int,
  view_type_name: String,
  note  :String,
  place_id :Int,
  active_flg :Boolean
)

case class ViewTypeData(
  viewTypeId: List[Int],
  viewTypeName: List[String]
)

@javax.inject.Singleton
class ViewTypeDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("view_type_id") ~
      get[String]("view_type_name")~
      get[String]("note") ~
      get[Int]("place_id") ~
      get[Boolean]("active_flg") map {
        case view_type_id ~ view_type_name  ~ note ~ place_id ~ active_flg=>
          ViewType(view_type_id, view_type_name,note ,place_id ,active_flg)
      }
  }

  def selectAll(): Seq[ViewType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select view_type_id, view_type_name ,note ,place_id ,active_flg
              from view_type order by view_type_id;
              """)
      sql.as(simple.*)
    }
  }

  def selectViewInfo(placeId: Int): Seq[ViewType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select view_type_id ,view_type_name ,note ,place_id ,active_flg
              from view_type
              where place_id = {placeId}
              and active_flg = true
              order by view_type_id;
              """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }
}