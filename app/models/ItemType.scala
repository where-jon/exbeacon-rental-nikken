package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._



//item_type_id serial NOT NULL,
//item_type_name text NOT NULL,
//item_type_category text NOT NULL,
//item_type_icon_color text NOT NULL,
//item_type_text_color text NOT NULL,
//item_type_row_color text NOT NULL,
//note text NOT NULL DEFAULT cast('' as text),
//place_id integer NOT NULL,
//active_flg boolean NOT NULL DEFAULT true,
//updatetime timestamp without time zone DEFAULT now(),
//CONSTRAINT item_type_pkey PRIMARY KEY (item_type_id)

case class ItemType(
  item_type_id: Int,
  item_type_name: String,
  item_type_category: String,
  item_type_icon_color: String,
  item_type_text_color: String,
  item_type_row_color: String,
  note: String,
  place_id :Int,
  active_flg: Boolean

)

case class ItemTypeData(
  viewTypeId: List[Int],
  viewTypeName: List[String]
)

@javax.inject.Singleton
class ItemTypeDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("item_type_category") ~
      get[String]("item_type_icon_color") ~
      get[String]("item_type_text_color") ~
      get[String]("item_type_row_color") ~
      get[String]("note") ~
      get[Int]("place_id") ~
      get[Boolean]("active_flg") map {
        case item_type_id ~ item_type_name ~ item_type_category ~ item_type_icon_color ~ item_type_text_color ~ item_type_row_color ~
          note ~ place_id ~ active_flg=>
          ItemType(item_type_id ,item_type_name ,item_type_category ,item_type_icon_color ,item_type_text_color ,item_type_row_color,
          note , place_id , active_flg)
      }
  }

  def selectAll(): Seq[ItemType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select item_type_id ,item_type_name ,item_type_category ,item_type_icon_color ,item_type_text_color ,item_type_row_color, note , place_id , active_flg
              from item_type order by item_type_id;
              """)
      sql.as(simple.*)
    }
  }
}