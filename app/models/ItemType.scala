package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db._

/*作業期間種別*/
case class WorkTypeEnum(
  map: Map[Int,String] =
  Map[Int,String](
    0 -> "午前",
    1 -> "午後",
    2 -> "終日")
)

/*仮設材種別*/
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

  def selectItemInfo(placeId: Int): Seq[ItemType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select item_type_id ,item_type_name ,item_type_category ,item_type_icon_color ,item_type_text_color ,item_type_row_color, note , place_id , active_flg
              from item_type
              where place_id = {placeId}
              and active_flg = true
              order by item_type_id;
              """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }

  /*カテゴリー名が作業車だけ検索する*/
  def selectItemCarInfo(placeId: Int): Seq[ItemType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select item_type_id ,item_type_name ,item_type_category ,item_type_icon_color ,item_type_text_color ,item_type_row_color, note , place_id , active_flg
              from item_type
              where place_id = {placeId}
              and active_flg = true
              and item_type_category = '作業車'
              order by item_type_id;
              """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }

  /*カテゴリー名が作業車だけ検索する*/
  def selectItemOtherInfo(placeId: Int): Seq[ItemType] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
              select item_type_id ,item_type_name ,item_type_category ,item_type_icon_color ,item_type_text_color ,item_type_row_color, note , place_id , active_flg
              from item_type
              where place_id = {placeId}
              and active_flg = true
              and item_type_category = 'その他'
              order by item_type_id;
              """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }
}