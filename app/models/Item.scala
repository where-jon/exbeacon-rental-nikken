package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.Logger
import play.api.db._


case class ActualItemInfo(
    itemId: Int
  , itemNo: String
  , itemBtxId: Int
)
case class ItemListInfo(
    itemKindId: Int
  , itemKindName: String
  , note: String
  , itemCount: Int
  , actualItemInfoList: Seq[ActualItemInfo]
)

@javax.inject.Singleton
class itemDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 仮設材情報の取得
    * @return
    */
  def selectItemInfo(placeId: Int, itemKindName: String = "", itemKindId: Option[Int] = None): Seq[ItemListInfo] = {

    val simple = {
      get[Int]("item_kind_id") ~
        get[String]("item_kind_name") ~
        get[String]("note") ~
        get[String]("actual_item_info_str") map {
        case item_kind_id ~ item_kind_name ~ note ~ actual_item_info_str  =>

          var actualItemInfoList = Seq[ActualItemInfo]()

          actual_item_info_str.split(",").toSeq.foreach(line =>{
            if(line.isEmpty == false){
              val array = line.split("\t")
              actualItemInfoList :+= ActualItemInfo(array(0).toInt, array(1), array(2).toInt)
            }
          })

          ItemListInfo(item_kind_id, item_kind_name, note, actualItemInfoList.length, actualItemInfoList)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              ik.item_kind_id
            , ik.item_kind_name
            , ik.note
            , ARRAY_TO_STRING(
                ARRAY(
                  SELECT
                      cast(it.item_id as text)
                      || CHR(9) || it.item_no
                      || CHR(9) || cast(it.item_btx_id as text)
                  FROM
                    item_table it
                  WHERE
                    it.item_kind_id = ik.item_kind_id
                  ORDER BY
                    it.item_id
                )
              , ',') as actual_item_info_str
          from
            place_master p
            inner join item_kind_master ik
              on p.place_id = ik.place_id
              and p.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(itemKindName.isEmpty == false){
        wherePh += s""" and ik.item_kind_name = '${itemKindName}' """
      }
      if(itemKindId != None){
        wherePh += s""" and ik.item_kind_id = ${itemKindId.get} """
      }

      val orderPh =
        """
          order by
            ik.item_kind_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  /**
    * 仮設材の削除
    * @return
    */
  def selectActualItemInfo(placeId: Int, includeItemKindId: Option[Int] = None, excludeItemKindId: Option[Int] = None): Seq[ActualItemInfo] ={
    
    db.withConnection { implicit connection =>

      val simple =
        get[Int]("item_id") ~
          get[String]("item_no") ~
          get[Int]("item_btx_id") map {
          case item_id ~ item_no ~ item_btx_id =>
            ActualItemInfo(item_id, item_no, item_btx_id)
        }
      
      val selectPh =
        s"""
          select
              it.item_id
            , it.item_no
            , it.item_btx_id
          from
            place_master p
            inner join item_kind_master ik
              on p.place_id = ik.place_id
            inner join item_table it
              on ik.item_kind_id = it.item_kind_id
          where
            p.place_id = ${placeId.toString}
            and p.active_flg = true
            and ik.active_flg = true
            and it.active_flg = true
        """
      var addWherePh = ""
      if(excludeItemKindId != None){
        addWherePh += s""" and ik.item_kind_id != ${excludeItemKindId.get.toString} """
      }
      if(includeItemKindId != None){
        addWherePh += s""" and ik.item_kind_id = ${includeItemKindId.get.toString} """
      }

      val orderPh = " order by it.item_kind_id, it.item_id"
Logger.debug(selectPh + addWherePh + orderPh)
      SQL(selectPh + addWherePh + orderPh).as(simple.*)
    }
  }

  /**
    * 仮設材の削除
    * @return
    */
  def deleteById(itemKindId:Int): Unit = {
    db.withTransaction { implicit connection =>

      // BTXマスタの取得
      val parser =
        get[Int]("item_btx_id") map {
          case item_kind_id => item_kind_id
        }
      val btxList: Seq[Int] = SQL(
        """
          select
            it.item_btx_id
          from
            item_table it
            inner join item_kind_master ik
              on it.item_kind_id = ik.item_kind_id
          where
            ik.item_kind_id = {itemKindId}
        """
      ).on('itemKindId -> itemKindId).as(parser.*)

      // BTXマスタの削除
      if(btxList.nonEmpty){
        SQL("""delete from btx_master where btx_id in ({btxList}) ;""").on('btxList -> btxList).executeUpdate()
      }
      // 仮設材マスタの削除
      SQL("""delete from item_kind_master where item_kind_id = {itemKindId} ;""").on('itemKindId -> itemKindId).executeUpdate()
      // 仮設材テーブルの削除
      SQL("""delete from item_table where item_kind_id = {itemKindId} ;""").on('itemKindId -> itemKindId).executeUpdate()
      // コミット
      connection.commit()

Logger.debug(s"""仮設材マスタを削除、ID：" + ${itemKindId.toString}""")
    }
  }

  /**
    * 仮設材の新規登録
    * @return
    */
  def insert(itemKindName:String, note:String, placeId: Int, lineList: Seq[String], btxList: Seq[Int]) = {

    db.withTransaction { implicit connection =>
      // 仮設材マスタへの登録
      val params: Seq[NamedParameter] = Seq(
        "itemKindName" -> itemKindName,
        "note" -> note,
        "placeId" -> placeId
      )
      var insertSql = SQL(
        """
          insert into item_kind_master (item_kind_name, note, place_id)
          values ({itemKindName}, {note}, {placeId})
        """
      ).on(params:_*)

      // SQL実行し、新規の仮設材種別IDを取得
      val newItemKindId: Option[Long] = insertSql.executeInsert()
      Logger.debug(s"""仮設材マスタを登録、ID：" + ${newItemKindId.get.toInt}""")

      // 仮設材テーブルへの登録
      val indexedValues = lineList.zipWithIndex

      val rows = indexedValues.map{ case (value, i) =>
          s"""({item_no_${i}}, {item_kind_id_${i}}, {item_btx_id_${i}})"""
      }.mkString(",")

      val parameters = indexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"item_no_${i}" , value.split(",")(0)),
          NamedParameter(s"item_kind_id_${i}" , newItemKindId.get.toInt),
          NamedParameter(s"item_btx_id_${i}" , value.split(",")(1).toInt)
        )
      }
      // SQL実行
      BatchSql(s""" insert into item_table (item_no, item_kind_id, item_btx_id) values ${rows} """, parameters).execute

      // BTXマスタへの登録
      val newIndexedValues = btxList.zipWithIndex

      val newRows = newIndexedValues.map{ case (value, i) =>
        s"""({btx_id_${i}}, {place_id_${i}})"""
      }.mkString(",")

      val newParameters = newIndexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"btx_id_${i}" , value),
          NamedParameter(s"place_id_${i}" , placeId)
        )
      }

      // SQL実行
      BatchSql(s""" insert into btx_master (btx_id, place_id) values ${newRows} """, newParameters).execute

      // コミット
      connection.commit()
      Logger.debug(s"""EXBマスタを登録""")
    }
  }


  /**
    * 仮設材情報の更新
    * @return
    */
  def update(itemKindId:Int, itemKindName:String, note:String, placeId: Int, lineList: Seq[String], paramBtxList: Seq[Int]) = {

    db.withTransaction { implicit connection =>

      // 仮設材マスタの更新
      val params: Seq[NamedParameter] = Seq(
        "itemKindName" -> itemKindName,
        "note" -> note,
        "itemKindId" -> itemKindId)
      SQL(
        """
          update item_kind_master
          set item_kind_name = {itemKindName}
          , note = {note}
          , updatetime = now()
          where item_kind_id = {itemKindId};
        """
      ).on(params:_*).executeUpdate()
      Logger.debug(s"""仮設材マスタを更新、ID：" + ${itemKindId}""")

      // BTXマスタの取得
      val parser =
        get[Int]("item_btx_id") map {
          case item_kind_id => item_kind_id
        }
      val dbBtxList: Seq[Int] = SQL(
        """
          select
            it.item_btx_id
          from
            item_table it
            inner join item_kind_master ik
              on it.item_kind_id = ik.item_kind_id
          where
            ik.item_kind_id = {itemKindId}
        """
      ).on('itemKindId -> itemKindId).as(parser.*)

      // BTXマスタの削除
      SQL("""delete from btx_master where btx_id in ({btxList}) ;""").on('btxList -> dbBtxList).executeUpdate()

      // 仮設材テーブルの削除
      SQL("""delete from item_table where item_kind_id = {itemKindId} ;""").on('itemKindId -> itemKindId).executeUpdate()


      // 仮設材テーブルへの登録
      val indexedValues = lineList.zipWithIndex

      val rows = indexedValues.map{ case (value, i) =>
        s"""({item_no_${i}}, {item_kind_id_${i}}, {item_btx_id_${i}})"""
      }.mkString(",")

      val parameters = indexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"item_no_${i}" , value.split(",")(0)),
          NamedParameter(s"item_kind_id_${i}" , itemKindId),
          NamedParameter(s"item_btx_id_${i}" , value.split(",")(1).toInt)
        )
      }
      // SQL実行
      BatchSql(s""" insert into item_table (item_no, item_kind_id, item_btx_id) values ${rows} """, parameters).execute

      // BTXマスタへの登録
      val newIndexedValues = paramBtxList.zipWithIndex

      val newRows = newIndexedValues.map{ case (value, i) =>
        s"""({btx_id_${i}}, {place_id_${i}})"""
      }.mkString(",")

      val newParameters = newIndexedValues.flatMap{ case(value, i) =>
        Seq(
          NamedParameter(s"btx_id_${i}" , value),
          NamedParameter(s"place_id_${i}" , placeId)
        )
      }

      // SQL実行
      BatchSql(s""" insert into btx_master (btx_id, place_id) values ${newRows} """, newParameters).execute

      // コミット
      connection.commit()
    }
  }

}

