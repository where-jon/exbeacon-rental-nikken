package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import play.api.Logger
import play.api.db._


case class Car(
  carId: Int,
  carNo: String,
  carName: String,
  carBtxId: Int,
  carKeyBtxId: Int,
  placeId: Int
)

case class ItemCar(
  carId: Int,
  itemTypeId: Int,
  carNo: String,
  carName: String,
  carBtxId: Int,
  carKeyBtxId: Int,
  placeId: Int
)

/*作業車・立馬一覧用formクラス*/
case class ItemCarData(
  itemTypeId: Int
)

@javax.inject.Singleton
class carDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
    get[Int]("item_car_id") ~
      get[String]("item_car_no") ~
      get[String]("item_car_name") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("item_car_key_btx_id") ~
      get[Int]("place_id") map {
      case item_car_id ~ item_car_no ~ item_car_name ~ item_car_btx_id ~ item_car_key_btx_id ~ place_id  =>
        Car(item_car_id, item_car_no, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
    }
  }

  val simple2 = {
    get[Int]("item_car_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_car_no") ~
      get[String]("item_car_name") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("item_car_key_btx_id") ~
      get[Int]("place_id") map {
      case item_car_id ~ item_type_id ~ item_car_no ~ item_car_name ~ item_car_btx_id ~ item_car_key_btx_id ~ place_id  =>
        ItemCar(item_car_id, item_type_id,item_car_no, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
    }
  }

  def selectCarMasterInfo(placeId: Int): Seq[ItemCar] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
		          select item_car_id, item_type_id,item_car_no,item_car_name,item_car_btx_id,item_car_key_btx_id,place_id
		          from item_car_master where place_id = {placeId} order by item_car_id ;
		          """).on(
        "placeId" -> placeId
      )
      sql.as(simple2.*)
    }
  }

  /**
    * 作業車情報の取得
    * @return
    */
  def selectCarInfo(placeId: Int, carNo: String = "", carId: Option[Int] = None): Seq[Car] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_car_id
              , c.item_car_no
              , c.item_car_name
              , c.item_car_btx_id
              , c.item_car_key_btx_id
              , c.place_id
          from
            place_master p
            inner join item_car_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(carNo.isEmpty == false){
        wherePh += s""" and c.item_car_no = '${carNo}' """
      }
      if(carId != None){
        wherePh += s""" and c.item_car_id = ${carId.get} """
      }
      val orderPh =
        """
          order by
            c.item_car_id
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
      SQL("""delete from item_car_master where item_car_id = {carId} ;""").on('carId -> carId).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""業者を削除、ID：" + ${carId.toString}""")
    }
  }


  /**
    * 作業車の新規登録
    * @return
    */
  def insert(carNo: String, carName: String, carBtxId: Int, carKeyBtxId: Int, placeId: Int): Int = {
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
      )
      // クエリ
      val sql = SQL(
        """
          insert into item_car_master (item_car_no, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
          values ({carNo}, {carName}, {carBtxId}, {carKeyBtxId}, {placeId})
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
          update item_car_master set
              item_car_no = {carNo}
            , item_car_name = {carName}
            , item_car_btx_id = {carBtxId}
            , item_car_key_btx_id = {carKeyBtxId}
            , updatetime = now()
          where item_car_id = {carId} ;
        """).on(
        'carNo -> carNo, 'carName -> carName, 'carBtxId -> carBtxId, 'carKeyBtxId -> carKeyBtxId, 'carId -> carId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""作業車情報を更新、ID：" + ${carId.toString}""")
    }
  }

}

