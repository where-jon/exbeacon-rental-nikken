package models

import javax.inject.Inject
import play.api.Logger
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

  def selectItemTypeInfo(placeId: Int): Seq[ItemType] = {
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

  /**
    * 仮設材種別の削除
    * @return
    */
  def deleteById(itemTypeId:Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL("""delete from item_type where item_type_id = {itemTypeId} ;""").on('itemTypeId -> itemTypeId).executeUpdate()
      // コミット
      connection.commit()
    }
  }

  /**
    * 仮設材種別の新規登録
    * @return
    */
  def insert(itemTypeName: String, itemTypeCategory: String, itemTypeIconColor: String, itemTypeTextColor: String, itemTypeRowColor: String, note: String, placeId: Int): Int = {
    db.withTransaction { implicit connection =>
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "itemTypeName" -> itemTypeName
        ,"itemTypeCategory" -> itemTypeCategory
        ,"itemTypeIconColor" -> itemTypeIconColor
        ,"itemTypeTextColor" -> itemTypeTextColor
        ,"itemTypeRowColor" -> itemTypeRowColor
        ,"note" -> note
        ,"placeId" -> placeId
      )
      // クエリ
      val sql = SQL(
        """
          insert into item_type (item_type_name, item_type_category, item_type_icon_color, item_type_text_color, item_type_row_color, note, place_id)
           values ({itemTypeName}, {itemTypeCategory}, {itemTypeIconColor}, {itemTypeTextColor}, {itemTypeRowColor}, {note}, {placeId})
        """)
        .on(params:_*)

      // SQL実行
      val id: Option[Long] = sql.executeInsert()

      // コミット
      connection.commit()
      id.get.toInt
    }
  }

  /**
    * 仮設材種別の更新
    * @return
    */
  def updateById(itemTypeId:Int, itemTypeName: String, itemTypeCategory: String, itemTypeIconColor: String, itemTypeTextColor: String, itemTypeRowColor: String, note: String): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update item_type set
              item_type_name = {itemTypeName}
            , item_type_category = {itemTypeCategory}
            , item_type_icon_color = {itemTypeIconColor}
            , item_type_text_color = {itemTypeTextColor}
            , item_type_row_color = {itemTypeRowColor}
            , note = {note}
            , updatetime = now()
          where item_type_id = {itemTypeId} ;
        """).on(
        'itemTypeName -> itemTypeName, 'itemTypeCategory -> itemTypeCategory, 'itemTypeIconColor -> itemTypeIconColor, 'itemTypeTextColor -> itemTypeTextColor, 'itemTypeRowColor -> itemTypeRowColor, 'note -> note, 'itemTypeId -> itemTypeId
      ).executeUpdate()

      // コミット
      connection.commit()
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