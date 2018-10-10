package models

import java.sql.SQLException

import javax.inject.Inject
import anorm.SqlParser._
import anorm.{~, _}
import controllers.site.{CancelItem, ReserveItem}
import play.api.Logger
import play.api.db._


/*作業車・立馬予約用formクラス*/
case class NewItemCarReserveData(
   checkList: List[String]
  , itemId: List[Int]
  , itemTypeIdList: List[Int]
  , dayList: List[String]
  , workTypeList: List[Int]
  , companyName: String
  , floorName: String
)

/*作業車・立馬予約用formクラス*/
case class ItemCarReserveData(
  itemTypeId: Int
  , workTypeName: String
  , inputDate: String
  , companyName: String
  , floorName: String
  , itemId: List[Int]
  , itemTypeIdList: List[Int]
  , checkVal: List[Int]

)

/*作業車・立馬予約取消用formクラス*/
case class ItemCarCancelData(
  itemTypeIdList: List[Int]
  , itemId: List[Int]
  , itemReserveIdList: List[Int]
  , workTypeNameList: List[String]
  , reserveStartDateList: List[String]
  , checkVal: List[Int]
)


/*作業車・立馬予約取消検索用formクラス*/
case class ItemCarCancelSearchData(
  itemTypeId: Int
  , workTypeName: String
  , companyName: String
  , inputDate: String
)

/*作業車・立馬予約検索用formクラス*/
case class ItemCarSearchData(
   itemTypeId: Int
   , workTypeName: String
   , inputDate: String
 )

/*作業車・立馬予約検索用formクラス*/
case class NewItemCarSearchData(
  inputDate: String
  ,inputSearchDate: String
  , inputName: String
)

case class ItemCar(
  itemCarId: Int
  , itemTypeId: Int
  , note: String
  , itemCarNo: String
  , itemCarName: String
  , itemCarBtxId: Int
  , itemCarKeyBtxId: Int
  , placeId: Int
)

/*作業車・立馬管理検索用*/
case class ItemCarViewer(
  itemCarId: Int
  , itemTypeId: Int
  , itemTypeName: String
  , itemCarNote: String
  , itemCarNo: String
  , itemCarName: String
  , itemCarBtxId: Int
  , itemCarKeyBtxId: Int
  , placeId: Int
)

/*作業車・立馬管理予約検索用*/
case class ReserveMasterCheck(
  itemId: Int
  , workTypeId: Int
  , reserveStartDate: String
  , reserveEndDate: String
)

case class CarViewer(
  item_car_id: Int
  , item_car_btx_id: Int
  , item_car_key_btx_id: Int
  , item_type_id: Int
  , item_type_name:String
  , note:String
  , item_car_no: String
  , item_car_name:String
  , place_id: Int
  , reserve_start_date:String
  , company_id: Int
  , company_name: String
  , work_type_id: Int
  , work_type_name: String
  , reserve_floor_name: String
  , reserve_id: Int
)

case class CarReserveViewer(
  item_car_id: Int
  , item_car_btx_id: Int
  , item_car_key_btx_id: Int
  , item_type_id: Int
  , item_car_no: String
  , item_car_name:String
  , place_id: Int
  , ar_reserve_date:Seq[String]
  , ar_reserve_company_name: Seq[String]
  , ar_reserve_work_type: Seq[String]
)

/*作業車・立馬一覧用formクラス*/
case class ItemCarData(
  itemTypeId: Int
  , companyName: String
  , floorName: String
  , workTypeName: String
)

@javax.inject.Singleton
class itemCarDAO @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  val carmanage = {
    get[Int]("item_car_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("note") ~
      get[String]("item_car_no") ~
      get[String]("item_car_name") ~
      get[Int]("item_car_btx_id") ~
      get[Int]("item_car_key_btx_id") ~
      get[Int]("place_id") map {
      case item_car_id ~ item_type_id ~ item_type_name ~ note ~ item_car_no ~ item_car_name ~ item_car_btx_id ~ item_car_key_btx_id ~ place_id  =>
        ItemCarViewer(item_car_id, item_type_id, item_type_name, note, item_car_no, item_car_name, item_car_btx_id, item_car_key_btx_id, place_id)
    }
  }

  /**
    * 作業車・立馬情報の取得
    * @return
    */
  def selectCarMasterInfo(placeId: Int): Seq[ItemCarViewer] = {
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
      sql.as(carmanage.*)
    }
  }

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

  /**
    * 作業車・立馬情報の取得
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
    * 作業車・立馬情報TagIDチェック用
    * @return
    */
  def selectCarTagCheck(placeId: Int, carId: Option[Int] = None, chkTagId: Int): Seq[ItemCar] = {

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
      if(carId != None){
        wherePh += s""" and c.item_car_id <> ${carId.get} """
      }
      wherePh += s""" and (c.item_car_btx_id = ${chkTagId} """
      wherePh += s""" or c.item_car_key_btx_id = ${chkTagId}) """

      val orderPh =
        """
          order by
            c.item_car_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  /**
    * 作業車・立馬の削除
    * @return
    */
  def delete(carId:Int, placeId: Int): Unit = {
    db.withTransaction { implicit connection =>

      // 作業車の削除
      SQL("""delete from item_car_master where item_car_id = {carId} ;""").on('carId -> carId).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""作業車・立馬を削除、ID：" + ${carId.toString}""")
    }
  }


  /**
    * 作業車・立馬のBtxIDと鍵BtxIDの存在チェック
    * @return
    */
  def selectCarBtxCheck(placeId: Int, carBtxId: Option[Int] = None, carKeyBtxId: Option[Int] = None): Seq[ItemCar] = {
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

      var wherePh =
        """
           where p.place_id = {placeId}
        """.stripMargin
      if(carBtxId != None){
        wherePh += s""" and c.item_car_btx_id = ${carBtxId.get} """
      }
      if(carKeyBtxId != None){
        wherePh += s""" and c.item_car_key_btx_id = ${carKeyBtxId.get} """
      }
      val orderPh =
        """
          order by
            c.item_car_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  val reserveMasterCheck = {
    get[Int]("item_id") ~
      get[Int]("work_type_id") ~
      get[String]("reserve_start_date")~
      get[String]("reserve_end_date") map {
      case item_id ~ work_type_id ~ reserve_start_date ~ reserve_end_date =>
        ReserveMasterCheck(item_id, work_type_id, reserve_start_date, reserve_end_date)
    }
  }
  /*作業車・立馬予約状況確認*/
  def selectCarReserveCheck(
                             placeId: Int,
                             carId: Int,
                             itemTypeId: Int
                           ): Seq[ReserveMasterCheck] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               r.item_id
             , r.work_type_id
             , to_char(r.reserve_start_date, 'YYYYMMDD') as reserve_start_date
             , to_char(r.reserve_end_date, 'YYYYMMDD') as reserve_end_date
          from
           place_master p
             inner join reserve_table r
               on p.place_id = r.place_id
               and p.active_flg = true
               and r.active_flg = true
          where
            r.active_flg = true
            and r.place_id = """  + {placeId} + """

        """
      // 追加検索条件
      var wherePh = ""
      wherePh += s""" and r.item_id = {carId} """
      wherePh += s""" and r.item_type_id = {itemTypeId} """

      // 表示順を設定
      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'carId -> carId, 'itemTypeId -> itemTypeId).as(reserveMasterCheck.*)
    }
  }

  /**
    * 作業車の新規登録
    * @return
    */
  def insert(carNo: String, carName: String, carBtxId: Int, carKeyBtxId: Int, itemTypeId: Int, note:String, placeId: Int): Int = {
    db.withTransaction { implicit connection =>
      // 作業車マスタの登録
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "carNo" -> carNo
        ,"carName" -> carName
        ,"carBtxId" -> carBtxId
        ,"carKeyBtxId" -> carKeyBtxId
        ,"placeId" -> placeId
        ,"itemTypeId" -> itemTypeId
        ,"note" -> note
      )
      // クエリ
      val sql = SQL(
        """
          insert into item_car_master (item_car_no,item_type_id, item_car_name, item_car_btx_id, item_car_key_btx_id, note, place_id)
          values ({carNo}, {itemTypeId}, {carName}, {carBtxId}, {carKeyBtxId}, {note}, {placeId})
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
    * 作業車・立馬の更新
    * @return
    */
  def update(carId:Int, carNo: String, carName: String, carBtxId:Int, carKeyBtxId:Int, itemTypeId:Int, note:String, placeId:Int,
             oldBtxId:Int, oldCarKeyBtxId:Int): Unit = {
    db.withTransaction { implicit connection =>

      // 作業車・立馬マスタの更新
      SQL(
        """
          update item_car_master set
              item_car_no = {carNo}
            , item_car_name = {carName}
            , item_car_btx_id = {carBtxId}
            , item_car_key_btx_id = {carKeyBtxId}
            , item_type_id = {itemTypeId}
            , note = {note}
            , updatetime = now()
          where item_car_id = {carId} ;
        """).on(
        'carNo -> carNo,
              'carName -> carName,
              'carBtxId -> carBtxId,
              'carKeyBtxId -> carKeyBtxId,
              'carId -> carId,
              'itemTypeId -> itemTypeId,
              'note -> note
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
             		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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
           and c.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
           order by item_car_btx_id ;

        """
      SQL(selectPh).as(carMasterViewer.*)
    }
  }



   /*作業車・立馬予約用 sql文 20180913*/
  def selectCarMasterCalendarType(placeId : Int,itemIdList:Seq[Int],startDate : String,endDate : String): Seq[CarReserveViewer] = {

    val carReserveViewer = {
        get[Int]("item_car_id") ~
        get[Int]("item_car_btx_id") ~
        get[Int]("item_car_key_btx_id") ~
        get[Int]("item_type_id") ~
        get[String]("item_car_no") ~
        get[String]("item_car_name") ~
        get[Int]("place_id") ~
        get[String]("ar_reserve_date") ~
        get[String]("ar_reserve_company_name") ~
        get[String]("ar_reserve_work_type") map {
        case item_car_id ~ item_car_btx_id ~ item_car_key_btx_id ~ item_type_id ~
          item_car_no ~item_car_name ~place_id ~
          ar_reserve_date ~ ar_reserve_company_name ~ar_reserve_work_type =>
          CarReserveViewer(item_car_id, item_car_btx_id, item_car_key_btx_id, item_type_id,
             item_car_no, item_car_name, place_id,
            ar_reserve_date.split(",").toSeq,ar_reserve_company_name.split(",").toSeq,ar_reserve_work_type.split(",").toSeq)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_car_id
               ,c.item_car_btx_id
               , c.item_car_key_btx_id
               , c.item_type_id
               , c.item_car_no
               , c.item_car_name
               , c.place_id
               , ARRAY_TO_STRING(
 					ARRAY(
 				     SELECT
 				       reserve.reserve_start_date
 				     FROM
 				     reserve_table reserve
 				     WHERE
 				     reserve.item_id = c.item_car_id
 				     and
 				     reserve.reserve_start_date  between to_date('""" + {startDate} +"""', 'YYYY-MM-DD') and
                                                  to_date('""".stripMargin + {endDate} +"""', 'YYYY-MM-DD')
 						and reserve.place_id = """  + {placeId} + """
            and reserve.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
 				 		ORDER BY
          			reserve.item_id, reserve_start_date
            	), ',') as ar_reserve_date
            	, ARRAY_TO_STRING(
 					ARRAY(
 				     select
 				       company.company_name
 				     FROM
 				     reserve_table reserve
 				     LEFT JOIN company_master as company on company.company_id = reserve.company_id
 				     WHERE
 				     reserve.item_id = c.item_car_id
            and
            reserve.reserve_start_date  between to_date('""" + {startDate} +"""', 'YYYY-MM-DD') and
                                                  to_date('""" + {endDate} +"""', 'YYYY-MM-DD')
 						and reserve.place_id = """  + {placeId} + """
            and reserve.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
 				 		ORDER BY
          			reserve.item_id, reserve_start_date
            	), ',') as ar_reserve_company_name
            	, ARRAY_TO_STRING(
 					ARRAY(
 				     select
 				     work.work_type_name
 				     FROM
 				     reserve_table reserve
 				     LEFT JOIN work_type as work on work.work_type_id = reserve.work_type_id
 				     where
 				     reserve.item_id = c.item_car_id
 				     and
 				     reserve.reserve_start_date  between to_date('""" + {startDate} +"""', 'YYYY-MM-DD') and
                                                 to_date('""" + {endDate} +"""', 'YYYY-MM-DD')
 						and reserve.place_id = """  + {placeId} + """
            and reserve.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
 				 		ORDER BY
          			reserve.item_id, reserve_start_date
            	), ',') as ar_reserve_work_type
         from
           item_car_master as c
              left JOIN item_type as i on i.item_type_id = c.item_type_id
	            and i.active_flg = true
            where c.place_id = """  + {placeId} + """
            and c.active_flg = true
            --and c.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
            order by item_car_id

        """
      SQL(selectPh).as(carReserveViewer.*)
    }
  }


  /*作業車・立馬登録初期空き情報用 sql文 20180718*/
  def selectCarMasterReserve2(placeId : Int,itemIdList:Seq[Int]): Seq[CarViewer] = {
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
              		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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
                --and coalesce(r.reserve_id, -1) = -1
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
              		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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
  def getCarMasterSearch(
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
             		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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
      wherePh +=
        s""" and c.place_id = ${placeId}
           and not r.item_id in
           (select r.item_id from reserve_table where work.work_type_name ='${workTypeName}')
           and reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD')
         """

      // 表示順を設定
      val orderPh =
        """
          order by
            c.item_car_btx_id
        """
      SQL(selectPh + wherePh + orderPh).as(carMasterViewer.*)
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
             		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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

      val vCount = this.getCarMasterSearch(placeId, itemTypeId, workTypeName, reserveDate, itemIdList).length
      if( vCount == 0 ){
        wherePh += s"""
            and not r.item_id in
            (select item_id from reserve_table where reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD'))  """
      }else{
        if(workTypeName == "終日"){
          wherePh += s"""
            and not r.item_id in
            (select item_id from reserve_table where reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD'))  """
        }else if (workTypeName == "") {
          wherePh += s"""
             and not r.item_id in
            (select item_id from reserve_table where reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD'))  """
        } else{
          wherePh +=
            s"""
             and not r.item_id in
            (select item_id from reserve_table where (work.work_type_name ='${workTypeName}' or work_type_id = 3) and reserve_start_date = to_date('${reserveDate}', 'YYYY-MM-DD')
            ) """
        }
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
      reserveItemCar.zipWithIndex.foreach { case (item, num) =>
        val sql = SQL("""

            insert into reserve_table
            (item_type_id, item_id, floor_id, place_id,company_id,reserve_start_date,reserve_end_date,active_flg,updatetime,work_type_id) values(
            {item_type_id}, {item_id}, {floor_id},{place_id},{company_id},to_date({reserve_start_date}, 'YYYY-MM-DD'),to_date({reserve_end_date}, 'YYYY-MM-DD'),true,now(),{work_type_id})

              """).on(
          'item_type_id -> item.item_type_id,
          'item_id -> item.item_id,
          'floor_id ->item.floor_id,
          'place_id ->item.place_id,
          'company_id ->item.company_id,
          'reserve_start_date->item.reserve_start_date,
          'reserve_end_date->item.reserve_end_date,
          'active_flg->item.active_flg,
          'work_type_id->item.work_type_id
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

  /*作業車・立馬予約取消用 sql文 20180727*/
  def cancelItemCar(cancelItem: List[CancelItem]): String = {
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      cancelItem.zipWithIndex.map { case (item, i) =>
        val sql = SQL("""
         delete from reserve_table where
         reserve_id =  """ + {item.item_reserve_id} + """
         and active_flg = """ + {item.active_flg} + """
         and place_id = """ + {item.place_id} + """
        """)

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


  /*作業車・立馬予約取消情報用 sql文 20180726*/
  def selectCarMasterCancel(placeId : Int,itemIdList:Seq[Int]): Seq[CarViewer] = {
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
              		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
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
                and coalesce(r.reserve_id, -1) != -1
                order by item_car_btx_id ;

        """
      SQL(selectPh).as(carMasterViewer.*)
    }
  }

  /*作業車・立馬予約取消情報用 初期表示用 sql文 20180726*/
  def selectCarMasterCancelInitDsp(placeId : Int,itemIdList:Seq[Int]): Seq[CarViewer] = {
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
              		LEFT JOIN reserve_table as r on c.item_car_id = r.item_id
              		and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
              		and r.active_flg = true
                 and r.reserve_start_date >= CURRENT_DATE
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
                and coalesce(r.reserve_id, -1) != -1
                order by item_car_btx_id ;

        """
      SQL(selectPh).as(carMasterViewer.*)
    }
  }

  /**
    * 作業車・立馬情報 仮設材種別チェック用
    * @return
    */
  def selectItemTypeCheck(placeId: Int, itemTypeId: Int): Seq[ItemCar] = {

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
          wherePh += s""" and c.item_type_id = {itemTypeId} """

      val orderPh =
        """
          order by
            c.item_car_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'itemTypeId -> itemTypeId).as(simple.*)
    }
  }

}

