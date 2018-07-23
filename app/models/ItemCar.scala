package models

import java.sql.SQLException
import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
import controllers.site.ReserveItem
import play.api.Logger
import play.api.db._

/*作業車・立馬予約用formクラス*/
case class ItemCarReserveData(
  itemTypeId: Int,
  workTypeName: String,
  inputDate: String,
  companyName: String,
  floorName: String,
  itemId: List[Int],
  checkVal: List[Int]

)

/*作業車・立馬予約検索用formクラス*/
case class ItemCarSearchData(
   itemTypeId: Int,
   workTypeName: String,
   inputDate: String
 )

case class ItemCar(
  itemCarId: Int,
  itemTypeId: Int,
  note: String,
  itemCarNo: String,
  itemCarName: String,
  itemCarBtxId: Int,
  itemCarKeyBtxId: Int,
  placeId: Int
)

case class CarViewer(
  item_car_id: Int,
  item_car_btx_id: Int,
  item_car_key_btx_id: Int,
  item_type_id: Int,
  item_type_name:String,
  note:String,
  item_car_no: String,
  item_car_name:String,
  place_id: Int,
  reserve_start_date:String,
  company_id: Int,
  company_name: String,
  work_type_id: Int,
  work_type_name: String,
  reserve_floor_name: String,
  reserve_id: Int
)

/*作業車・立馬一覧用formクラス*/
case class ItemCarData(
  itemTypeId: Int,
  companyName: String,
  floorName: String,
  workTypeName: String
)

@javax.inject.Singleton
class itemCarDAO @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
    get[Int]("item_car_id") ~
      get[Int]("item_type_id") ~
      get[String]("note") ~
      get[String]("item_car_no") ~
      get[String]("item_car_name") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("item_car_key_btx_id") ~
      get[Int]("place_id") map {
      case item_car_id ~ item_type_id ~ note ~ item_car_no ~ item_car_name ~ item_car_btx_id ~ item_car_key_btx_id ~ place_id  =>
        ItemCar(item_car_id, item_type_id, note, item_car_no, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
    }
  }

  def selectCarMasterInfo(placeId: Int): Seq[ItemCar] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
            select
                 c.item_car_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_car_no
               , c.item_car_name
               , c.item_car_btx_id
               , c.item_car_key_btx_id
               , c.place_id
           from
             item_type i
             inner join item_car_master c
               on i.item_type_id = c.item_type_id
               and i.active_flg = true
               and c.active_flg = true
		           where c.place_id = {placeId} order by item_car_id ;
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
  def selectCarInfo(placeId: Int, carNo: String = "", carId: Option[Int] = None): Seq[ItemCar] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_car_id
              , c.item_type_id
              , c.note
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
          insert into item_car_master (item_car_no,item_type_id, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
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


  val carMasterViewer = {
    get[Int]("item_car_id") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("item_car_key_btx_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("note") ~
      get[String]("item_car_no") ~
      get[String]("item_car_name") ~
      get[Int]("place_id") ~
      get[String]("reserve_start_date") ~
      get[Int]("company_id") ~
      get[String]("company_name") ~
      get[Int]("work_type_id") ~
      get[String]("work_type_name") ~
      get[String]("reserve_floor_name") ~
      get[Int]("reserve_id")map {
      case item_car_id ~ item_car_btx_id ~ item_car_key_btx_id ~ item_type_id ~ item_type_name ~
        note ~ item_car_no ~item_car_name ~place_id ~reserve_start_date ~company_id ~company_name ~work_type_id ~
        work_type_name ~reserve_floor_name ~ reserve_id  =>
        CarViewer(item_car_id, item_car_btx_id, item_car_key_btx_id, item_type_id, item_type_name,
          note, item_car_no, item_car_name, place_id, reserve_start_date, company_id,company_name,work_type_id,
          work_type_name,reserve_floor_name,reserve_id)
    }
  }

  /*作業車・立馬一覧用 sql文 20180718*/
  def selectCarMasterViewer(placeId: Int,itemIdList:Seq[Int]): Seq[CarViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_car_id
               ,c.item_car_btx_id
               , c.item_car_key_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_car_no
               , c.item_car_name
               , c.place_id
               ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), 'date') as reserve_start_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '無') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
           from
             		item_car_master as c
             		LEFT JOIN reserve_table_new as r on c.item_car_id = r.item_id
                and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
                and to_char(r.reserve_start_date, 'YYYY-MM-DD') = to_char(current_timestamp, 'YYYY-MM-DD')
             		and r.active_flg = true
						left JOIN item_type as i on i.item_type_id = c.item_type_id
	             		and i.active_flg = true
		             		left JOIN company_master as co on co.company_id = r.company_id
		             		and co.active_flg = true
			             		left JOIN work_type as work on work.work_type_id = r.work_type_id
			             		and work.active_flg = true
			             			left JOIN floor_master as floor on floor.floor_id = r.floor_id
				             		and floor.active_flg = true
           where c.place_id = """  + {placeId} + """
           and c.active_flg = true
           order by item_car_btx_id ;

        """
      SQL(selectPh).as(carMasterViewer.*)
    }
  }


  /*作業車・立馬登録初期空き情報用 sql文 20180718*/
  def selectCarMasterReserve(placeId : Int,itemIdList:Seq[Int]): Seq[CarViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                 c.item_car_id
                ,c.item_car_btx_id
                , c.item_car_key_btx_id
                , c.item_type_id
                , i.item_type_name
                , c.note
                , c.item_car_no
                , c.item_car_name
                , c.place_id
                ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
                ,coalesce(r.company_id, -1) as company_id
                ,coalesce(co.company_name, '無') as company_name
                ,coalesce(work.work_type_id, -1) as work_type_id
                ,coalesce(work.work_type_name, '未予約') as work_type_name
                ,coalesce(floor.floor_name, '無') as reserve_floor_name
                ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_car_master as c
              		LEFT JOIN reserve_table_new as r on c.item_car_id = r.item_id
              		and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
              		and r.active_flg = true
 						left JOIN item_type as i on i.item_type_id = c.item_type_id
 	             		and i.active_flg = true
 		             		left JOIN company_master as co on co.company_id = r.company_id
 		             		and co.active_flg = true
 			             		left JOIN work_type as work on work.work_type_id = r.work_type_id
 			             		and work.active_flg = true
 			             			left JOIN floor_master as floor on floor.floor_id = r.floor_id
 				             		and floor.active_flg = true
                where c.place_id = """  + {placeId} + """
                and c.active_flg = true
                and coalesce(r.reserve_id, -1) = -1
                order by item_car_btx_id ;

        """
      SQL(selectPh).as(carMasterViewer.*)
    }
  }

  /*作業車・立馬登録空き情報検索用 sql文 20180719*/
  def selectCarMasterSearch(
                      placeId: Int,
                      itemTypeId: Int,
                      workTypeName: String,
                      reserveDate: String,
                      itemIdList:Seq[Int]
                   ): Seq[CarViewer] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               c.item_car_id
               ,c.item_car_btx_id
               , c.item_car_key_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_car_no
               , c.item_car_name
               , c.place_id
              ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '未予約') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_car_master as c
             		LEFT JOIN reserve_table_new as r on c.item_car_id = r.item_id
                and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
             		and r.active_flg = true
                and r.place_id = """  + {placeId} + """
                left JOIN item_type as i on i.item_type_id = c.item_type_id
                and i.active_flg = true
                  left JOIN company_master as co on co.company_id = r.company_id
                  and co.active_flg = true
                    left JOIN work_type as work on work.work_type_id = r.work_type_id
                    and work.active_flg = true
                      left JOIN floor_master as floor on floor.floor_id = r.floor_id
                      and floor.active_flg = true
          where
            c.active_flg = true

        """
      // 追加検索条件
      var wherePh = ""
      wherePh += s""" and c.place_id = ${placeId} """

      if(workTypeName == "終日" || workTypeName == ""){
        wherePh += s""" and r.reserve_start_date != to_date('${reserveDate}', 'YYYY-MM-DD') """
      }else{
        wherePh +=
          s"""  and r.reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD') and not work.work_type_name = '${workTypeName}' and not work.work_type_name = '終日'
                         or r.reserve_start_date != to_date('${reserveDate}', 'YYYY-MM-DD') and not work.work_type_name = '${workTypeName}' and not work.work_type_name = '終日' """
      }
      wherePh += s""" or coalesce(r.reserve_id, -1) = -1 and c.active_flg = true and c.place_id = ${placeId} """

      // 表示順を設定
      val orderPh =
        """
          order by
            c.item_car_btx_id
        """
      SQL(selectPh + wherePh + orderPh).as(carMasterViewer.*)
    }
  }


  def reserveItemCar(reserveItemCar: List[ReserveItem]): String = {
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      //reserveItemCar(1).itemTypeId
      val statement = connection.createStatement()
      var num = 0
      var vEndPoint = reserveItemCar.length - 1;
      for (num <- 0 to vEndPoint) {
        val sql = SQL("""

            insert into reserve_table_new
            (item_type_id, item_id, floor_id, place_id,company_id,reserve_start_date,reserve_end_date,active_flg,updatetime,work_type_id) values(
            {item_type_id}, {item_id}, {floor_id},{place_id},{company_id},to_date({reserve_start_date}, 'YYYY-MM-DD'),to_date({reserve_end_date}, 'YYYY-MM-DD'),true,now(),{work_type_id})

              """).on(
          'item_type_id -> reserveItemCar(num).item_type_id,
          'item_id -> reserveItemCar(num).item_id,
          'floor_id ->reserveItemCar(num).floor_id,
          'place_id ->reserveItemCar(num).place_id,
          'company_id ->reserveItemCar(num).company_id,
          'reserve_start_date->reserveItemCar(num).reserve_start_date,
          'reserve_end_date->reserveItemCar(num).reserve_end_date,
          'active_flg->reserveItemCar(num).active_flg,
          'work_type_id->reserveItemCar(num).work_type_id
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error " + e)
            vResult = e + ""
          }
        }
      }

    }
    vResult
  }


}

