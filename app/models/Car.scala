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

/*作業車・立馬一覧用formクラス*/
case class CarData(
  placeId: Int
)

@javax.inject.Singleton
class carDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
    get[Int]("car_id") ~
      get[String]("car_no") ~
      get[String]("car_name") ~
      get[Int]("car_btx_id") ~
      get[Int]("car_key_btx_id") ~
      get[Int]("place_id") map {
      case car_id ~ car_no ~ car_name ~ car_btx_id ~ car_key_btx_id ~ place_id  =>
        Car(car_id, car_no, car_name, car_btx_id, car_key_btx_id, place_id)
    }
  }

  def selectCarMasterAll(): Seq[Car] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
		          select car_id,car_no,car_name,car_btx_id,car_key_btx_id,place_id
		          from car_master order by car_id;
		          """)

      sql.as(simple.*)
    }
  }

  def selectCarMasterInfo(placeId: Int): Seq[Car] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
		          select car_id,car_no,car_name,car_btx_id,car_key_btx_id,place_id
		          from car_master where place_id = {placeId} order by car_id ;
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
  def selectCarInfo(placeId: Int, carNo: String = "", carId: Option[Int] = None): Seq[Car] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.car_id
              , c.car_no
              , c.car_name
              , c.car_btx_id
              , c.car_key_btx_id
              , c.place_id
          from
            place_master p
            inner join car_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(carNo.isEmpty == false){
        wherePh += s""" and c.car_no = '${carNo}' """
      }
      if(carId != None){
        wherePh += s""" and c.car_id = ${carId.get} """
      }
      val orderPh =
        """
          order by
            c.car_id
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
      SQL("""delete from car_master where car_id = {carId} ;""").on('carId -> carId).executeUpdate()

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
          insert into car_master (car_no, car_name, car_btx_id, car_key_btx_id, place_id)
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
          update car_master set
              car_no = {carNo}
            , car_name = {carName}
            , car_btx_id = {carBtxId}
            , car_key_btx_id = {carKeyBtxId}
            , updatetime = now()
          where car_id = {carId} ;
        """).on(
        'carNo -> carNo, 'carName -> carName, 'carBtxId -> carBtxId, 'carKeyBtxId -> carKeyBtxId, 'carId -> carId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""作業車情報を更新、ID：" + ${carId.toString}""")
    }
  }

}

