package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

case class ViewType(
  view_type_id: Int,
  view_type_name: String
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
      get[String]("view_type_name") map {
        case view_type_id ~ view_type_name =>
          ViewType(view_type_id, view_type_name)
      }
  }

  def selectAll(): Seq[ViewType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select view_type_id, view_type_name
              from view_type_master order by view_type_id;
              """)
      sql.as(simple.*)
    }
  }
}