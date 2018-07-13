package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.Logger
import play.api.db._

case class ItemOther(
  itemOtherId: Int,
  itemTypeId: Int,
  note: String,
  itemOtherNo: String,
  itemOtherName: String,
  itemOtherBtxId: Int,
  placeId: Int
)

case class OtherViewer(
  item_other_id: Int,
  item_other_btx_id: Int,
  item_type_id: Int,
  item_type_name:String,
  note:String,
  item_other_no: String,
  item_other_name:String,
  place_id: Int,
  reserve_start_date:String,
  company_id: Int,
  company_name: String,
  work_type_id: Int,
  work_type_name: String,
  floor_name: String,
  reserve_id: Int
)

case class OtherViewer2(
  item_other_id: Int
)

/*作業車・立馬一覧用formクラス*/
case class ItemOtherData(
  itemTypeId: Int,
  companyName: String,
  floorName: String,
  workTypeName: String
)

@javax.inject.Singleton
class itemOtherDAO @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
    get[Int]("item_other_id") ~
      get[Int]("item_type_id") ~
      get[String]("note") ~
      get[String]("item_other_no") ~
      get[String]("item_other_name") ~
      get[Int]("item_other_btx_id") ~
      get[Int]("place_id") map {
      case item_other_id ~ item_type_id ~ note ~ item_other_no ~ item_other_name ~ item_other_btx_id ~  place_id  =>
        ItemOther(item_other_id, item_type_id, note, item_other_no, item_other_name, item_other_btx_id, place_id)
    }
  }

  def selectOtherMasterInfo(placeId: Int): Seq[ItemOther] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
            select
                 c.item_other_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_other_no
               , c.item_other_name
               , c.item_other_btx_id
               , c.place_id
           from
             item_type i
             inner join item_other_master c
               on i.item_type_id = c.item_type_id
               and i.active_flg = true
               and c.active_flg = true
		           where c.place_id = {placeId} order by item_other_id ;
		          """).on(
        "placeId" -> placeId
      )
      sql.as(simple.*)
    }
  }

  /**
    * 作業車情報の取得
    * @return
    */
  def selectOtherInfo(placeId: Int, carNo: String = "", carId: Option[Int] = None): Seq[ItemOther] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_other_id
              , c.item_type_id
              , c.note
              , c.item_other_no
              , c.item_other_name
              , c.item_other_btx_id
              , c.place_id
          from
            place_master p
            inner join item_other_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(carNo.isEmpty == false){
        wherePh += s""" and c.item_other_no = '${carNo}' """
      }
      if(carId != None){
        wherePh += s""" and c.item_other_id = ${carId.get} """
      }
      val orderPh =
        """
          order by
            c.item_other_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }


  /**
    * 作業車の削除
    * @return
    */
  def delete(carId:Int, placeId: Int, btxIdList: Seq[Int]): Unit = {
    db.withTransaction { implicit connection =>

      // Txの削除
      SQL(
        """
          delete from btx_master where place_id = {placeId} and btx_id in ({btxIdList}) ;
        """)
        .on('placeId -> placeId, 'btxIdList -> btxIdList).executeUpdate()

      // 作業車の削除
      SQL("""delete from item_other_master where item_other_id = {carId} ;""").on('carId -> carId).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""業者を削除、ID：" + ${carId.toString}""")
    }
  }


  /**
    * 作業車の新規登録
    * @return
    */
  def insert(carNo: String, carName: String, carBtxId: Int, carKeyBtxId: Int, placeId: Int, itemTypeId: Int): Int = {
    db.withTransaction { implicit connection =>

      // BTXマスタの登録
      val btxList = Seq[Int](carBtxId, carKeyBtxId)
      btxList.foreach( btxId =>{
        SQL("""insert into btx_master (btx_id, place_id) values ({btxId}, {placeId})""")
          .on('btxId -> btxId, 'placeId -> placeId).executeInsert()
      })

      // 作業車マスタの登録
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "carNo" -> carNo
        ,"carName" -> carName
        ,"carBtxId" -> carBtxId
        ,"carKeyBtxId" -> carKeyBtxId
        ,"placeId" -> placeId
        ,"itemTypeId" -> itemTypeId
      )
      // クエリ
      val sql = SQL(
        """
          insert into item_other_master (item_other_no,item_type_id, item_other_name, item_other_btx_id, place_id)
          values ({carNo}, {itemTypeId}, {carName}, {carBtxId}, {carKeyBtxId}, {placeId})
        """)
        .on(params:_*)

      // SQL実行
      val id: Option[Long] = sql.executeInsert()

      // コミット
      connection.commit()

      Logger.debug("作業車を新規登録、ID：" + id.get.toString)

      id.get.toInt
    }
  }

  /**
    * 業者の更新
    * @return
    */
  def update(carId:Int, carNo: String, carName: String, carBtxId:Int, carKeyBtxId:Int, placeId:Int,
             oldBtxId:Int, oldCarKeyBtxId:Int): Unit = {
    db.withTransaction { implicit connection =>

      // 古い情報でのBTXマスタ更新リスト
      val oldBtxList = Seq[Int](oldBtxId, oldCarKeyBtxId)
      // 削除
      SQL(
        """
          delete from btx_master where btx_id in ({btxIdList}) ;
        """)
        .on('btxIdList -> oldBtxList).executeUpdate()

      // 新しい情報でのBTXマスタ更新リスト
      val newBtxList = Seq[Int](carBtxId, carKeyBtxId)

      // 登録
      newBtxList.foreach( btxId =>{
        SQL("""insert into btx_master (btx_id, place_id) values ({btxId}, {placeId})""")
          .on('btxId -> btxId, 'placeId -> placeId).executeInsert()
      })


      // 作業車マスタの更新
      SQL(
        """
          update item_other_master set
              item_other_no = {carNo}
            , item_other_name = {carName}
            , item_other_btx_id = {carBtxId}
            , updatetime = now()
          where item_other_id = {carId} ;
        """).on(
        'carNo -> carNo, 'carName -> carName, 'carBtxId -> carBtxId, 'carKeyBtxId -> carKeyBtxId, 'carId -> carId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""作業車情報を更新、ID：" + ${carId.toString}""")
    }
  }


  val carMasterViewer = {
    get[Int]("item_other_id") ~
      get[Int]("item_other_btx_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("note") ~
      get[String]("item_other_no") ~
      get[String]("item_other_name") ~
      get[Int]("place_id") ~
      get[String]("reserve_start_date") ~
      get[Int]("company_id") ~
      get[String]("company_name") ~
      get[Int]("work_type_id") ~
      get[String]("work_type_name") ~
      get[String]("floor_name") ~
      get[Int]("reserve_id")map {
      case item_other_id ~ item_other_btx_id ~  item_type_id ~ item_type_name ~
        note ~ item_other_no ~item_other_name ~place_id ~reserve_start_date ~company_id ~company_name ~work_type_id ~
        work_type_name ~floor_name ~ reserve_id  =>
        OtherViewer(item_other_id, item_other_btx_id, item_type_id, item_type_name,
          note, item_other_no, item_other_name, place_id, reserve_start_date, company_id,company_name,work_type_id,
          work_type_name,floor_name,reserve_id)
    }
  }

  val carMasterViewer2 = {

    get[Int]("item_other_id") map {
      case item_other_id  =>
        OtherViewer2(item_other_id)
    }
  }


  def selectOtherMasterViewer(placeId: Int): Seq[OtherViewer2] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
            select
                c.item_other_id
           from
             		item_other_master as c
           where c.place_id = {placeId}
           order by item_other_id ;

		          """).on(
        "placeId" -> placeId
      )
      sql.as(carMasterViewer2.*)
    }
  }

}

