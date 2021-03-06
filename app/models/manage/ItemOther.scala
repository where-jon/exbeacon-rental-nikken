package models.manage

import java.sql.SQLException

import anorm.SqlParser.get
import anorm.{NamedParameter, SQL, ~}
import controllers.site.{CancelItem, ReserveItem}
import javax.inject.Inject
import play.api.Logger
import play.api.db.DBApi

// フォーム定義
case class ItemDeleteForm(
  deleteItemOtherId: String
  , deleteItemTypeId: String
)
case class ItemUpdateForm(
    inputPlaceId: String
  , inputItemOtherId: String
  , inputItemOtherBtxId: String
  , inputItemOtherNo: String
  , inputItemOtherName: String
  , inputItemNote: String
  , inputItemTypeName: String
  , inputItemTypeId: String
)



/*その他仮設材予約取消用formクラス*/
case class ItemOtherCancelData(
  itemTypeIdList: List[Int]
  , itemId: List[Int]
  , itemReserveIdList: List[Int]
  , workTypeNameList: List[String]
  , reserveStartDateList: List[String]
  , checkVal: List[Int]
)


/*その他仮設材予約取消検索用formクラス*/
case class ItemOtherCancelSearchData(
  itemTypeId: Int
  , workTypeName: String
  , companyName: String
  , inputStartDate: String
  , inputEndDate: String
)


/*その他仮設材予約用formクラス*/
case class ItemOtherReserveData(
  itemTypeId: Int
  , workTypeName: String
  , inputStartDate: String
  , inputEndDate: String
  , companyName: String
  , floorName: String
  , itemId: List[Int]
  , itemTypeIdList: List[Int]
  , checkVal: List[Int]

)

/*その他仮設材検索用formクラス*/
case class ItemOtherSearchData(
  itemTypeId: Int
  , workTypeName: String
  , inputStartDate: String
  , inputEndDate: String
)

/*その他仮設材管理検索用*/
case class ItemOther(
  itemOtherId: Int
  , itemTypeId: Int
  , note: String
  , itemOtherNo: String
  , itemOtherName: String
  , itemOtherBtxId: Int
  , placeId: Int
)

/*その他仮設材管理用*/
case class ItemOtherViewer(
  itemOtherId: Int
  , itemTypeId: Int
  , itemTypeName: String
  , itemOtherNote: String
  , itemOtherNo: String
  , itemOtherName: String
  , itemOtherBtxId: Int
  , placeId: Int
)

case class OtherViewer(
  item_other_id: Int
  , item_other_btx_id: Int
  , item_type_id: Int
  , item_type_name:String
  , note:String
  , item_other_no: String
  , item_other_name:String
  , place_id: Int
  , reserve_start_date:String
  , reserve_end_date:String
  , company_id: Int
  , company_name: String
  , var work_type_id: Int
  , var work_type_name: String
  , reserve_floor_name: String
  , reserve_id: Int
)

/*作業車・立馬一覧用formクラス*/
case class ItemOtherData(
  itemTypeId: Int
  , companyName: String
  , floorName: String
  , workTypeName: String
)

/*作業車・立馬一覧用formクラス*/
case class ItemTypeSerect(
  itemTypeId: Int
  , companyName: String
  , floorName: String
  , workTypeName: String
)


@javax.inject.Singleton
class ItemOtherDAO @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  val itemmanage = {
    get[Int]("item_other_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("note") ~
      get[String]("item_other_no") ~
      get[String]("item_other_name") ~
      get[Int]("item_other_btx_id") ~
      get[Int]("place_id") map {
      case item_other_id ~ item_type_id ~ item_type_name ~ note ~ item_other_no ~ item_other_name ~ item_other_btx_id ~  place_id  =>
        ItemOtherViewer(item_other_id, item_type_id, item_type_name, note, item_other_no, item_other_name, item_other_btx_id, place_id)
    }
  }
  /**
    * その他仮設材情報の取得
    * @return
    */
  def selectOtherMasterInfo(placeId: Int): Seq[ItemOtherViewer] = {
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
		           where i.item_type_category_id = 2 and c.place_id = {placeId}
		           order by item_other_id ;
		          """).on(
        "placeId" -> placeId
      )
      sql.as(itemmanage.*)
    }
  }

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

  /**
    * その他仮設材情報の取得
    * @return
    */
  def selectOtherInfo(
   placeId: Int
   , itemotherNo: String
   , itemotherId: Option[Int] = None
   , itemOtherBtxId: Option[Int] = None): Seq[ItemOther] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_other_id
              , c.item_type_id
              , c.item_other_btx_id
              , c.item_other_no
              , c.item_other_name
              , c.note
              , c.place_id
          from
            place_master p
            inner join item_other_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(itemotherNo.isEmpty == false){
        wherePh += s""" and c.item_other_no = '${itemotherNo}' """
      }
      if(itemotherId != None){
        wherePh += s""" and c.item_other_id = ${itemotherId.get} """
      }
      if(itemOtherBtxId != None){
        wherePh += s""" and c.item_other_btx_id = ${itemOtherBtxId.get} """
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
    * 仮設材の削除
    * @return
    */
  def delete(itemOtherId:Int, placeId: Int): Unit = {
    db.withTransaction { implicit connection =>

      // 作業車の削除
      SQL("""delete from item_other_master where item_other_id = {itemOtherId} ;""").on('itemOtherId -> itemOtherId).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""仮設材を削除、ID：" + ${itemOtherId.toString}""")
    }
  }

  /**
    * 仮設材のBtxID
    * @return
    */
  def selectItemOtherBtxListBtxCheck(placeId: Int, itemOtherBtxId: Int): Seq[ItemOther] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.item_other_id
               , c.item_type_id
               , c.item_other_btx_id
               , c.item_other_no
               , c.item_other_name
               , c.note
               , c.place_id
          from
            place_master p
            inner join item_other_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh =
        """
           where p.place_id = {placeId}
           and c.item_other_btx_id = {itemOtherBtxId}
        """.stripMargin
      val orderPh =
        """
          order by
            c.item_other_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'itemOtherBtxId -> itemOtherBtxId).as(simple.*)
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
                             itemOtherId: Int,
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
      wherePh += s""" and r.item_id = {itemOtherId} """
      wherePh += s""" and r.item_type_id = {itemTypeId} """

      // 表示順を設定
      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'itemOtherId -> itemOtherId, 'itemTypeId -> itemTypeId).as(reserveMasterCheck.*)
    }
  }

  /**
    * 仮設材の新規登録
    * @return
    */
  def insert(
              ItemOtherNo: String,
              ItemOtherName: String,
              ItemOtherBtxId: Int,
              ItemOtherNote: String,
              ItemTypeId: Int,
              placeId: Int): Int = {
    db.withTransaction { implicit connection =>
      // 作業車マスタの登録
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "ItemOtherNo" -> ItemOtherNo
        ,"ItemTypeId" -> ItemTypeId
        ,"ItemOtherName" -> ItemOtherName
        ,"ItemOtherBtxId" -> ItemOtherBtxId
        ,"ItemOtherNote" -> ItemOtherNote
        ,"placeId" -> placeId
      )
      // クエリ
      val sql = SQL(
        """
          insert into item_other_master (item_other_no,item_type_id, item_other_name, item_other_btx_id, note, place_id)
          values ({ItemOtherNo}, {ItemTypeId}, {ItemOtherName}, {ItemOtherBtxId}, {ItemOtherNote}, {placeId})
        """)
        .on(params:_*)

      // SQL実行
      val id: Option[Long] = sql.executeInsert()

      // コミット
      connection.commit()

      Logger.debug("仮設材を新規登録、ID：" + id.get.toString)

      id.get.toInt
    }
  }

  /**
    * その他仮設材情報の更新
    * @return
    */
  def update(
              ItemOtherId:Int,
              ItemOtherNo: String,
              ItemOtherName: String,
              ItemOtherBtxId:Int,
              ItemOtherNote:String,
              ItemTypeId:Int,
              placeId:Int): Unit = {
    db.withTransaction { implicit connection =>

      // その他仮設材マスタの更新
      SQL(
        """
          update item_other_master set
                    item_type_id = {ItemTypeId}
                  , item_other_btx_id = {ItemOtherBtxId}
                  , item_other_no = {ItemOtherNo}
                  , item_other_name = {ItemOtherName}
                  , note = {ItemOtherNote}
                  , updatetime = now()
          where item_other_id = {ItemOtherId} ;
        """).on(
        'ItemTypeId -> ItemTypeId,
              'ItemOtherBtxId -> ItemOtherBtxId,
              'ItemOtherNo -> ItemOtherNo,
              'ItemOtherName -> ItemOtherName,
              'ItemOtherNote -> ItemOtherNote,
              'ItemOtherId -> ItemOtherId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""その他仮設材情報を更新、ID：" + ${ItemOtherId.toString}""")
    }
  }


  val otherMasterViewer = {
    get[Int]("item_other_id") ~
      get[Int]("item_other_btx_id") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("note") ~
      get[String]("item_other_no") ~
      get[String]("item_other_name") ~
      get[Int]("place_id") ~
      get[String]("reserve_start_date") ~
      get[String]("reserve_end_date") ~
      get[Int]("company_id") ~
      get[String]("company_name") ~
      get[Int]("work_type_id") ~
      get[String]("work_type_name") ~
      get[String]("reserve_floor_name") ~
      get[Int]("reserve_id")map {
      case item_other_id ~ item_other_btx_id ~  item_type_id ~ item_type_name ~
        note ~ item_other_no ~item_other_name ~place_id ~reserve_start_date ~ reserve_end_date~ company_id ~company_name ~work_type_id ~
        work_type_name ~reserve_floor_name ~ reserve_id  =>
        OtherViewer(item_other_id, item_other_btx_id, item_type_id, item_type_name,
          note, item_other_no, item_other_name, place_id, reserve_start_date, reserve_end_date, company_id,company_name,work_type_id,
          work_type_name,reserve_floor_name,reserve_id)
    }
  }

  /*その他仮設材一覧用 sql文 20180718*/
  def selectOtherMasterSql(placeId: Int,itemIdList:Seq[Int]): Seq[OtherViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                 c.item_other_id
               , c.item_other_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_other_no
               , c.item_other_name
               , c.place_id
               ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
               ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '無') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
           from
             		item_other_master as c
             		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
                  and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
                  and (to_char(r.reserve_start_date, 'YYYY-MM-DD') <= to_char(current_timestamp, 'YYYY-MM-DD')
                  and to_char(r.reserve_end_date, 'YYYY-MM-DD') >= to_char(current_timestamp, 'YYYY-MM-DD'))
             		and r.active_flg = true
						left JOIN item_type as i on i.item_type_id = c.item_type_id
	             		and i.active_flg = true
		             		left JOIN company_master as co on co.company_id = r.company_id
		             		and co.active_flg = true
			             		left JOIN work_type as work on work.work_type_id = r.work_type_id
			             		and work.active_flg = true
			             			left JOIN floor_master as floor on floor.floor_id = r.floor_id
				             		and floor.active_flg = true
           where c.place_id= """  + {placeId} + """
           order by item_other_btx_id ;

        """
      SQL(selectPh).as(otherMasterViewer.*)

    }
  }

  /*その他仮設材一覧用 sql文 20180718*/
  def selectOtherMasterViewer(placeId: Int,itemIdList:Seq[Int]): Seq[OtherViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                 c.item_other_id
               , c.item_other_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_other_no
               , c.item_other_name
               , c.place_id
               ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
               ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '無') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
           from
             		item_other_master as c
             		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id and r.work_type_id = c.item_type_id
                  and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
                  and (to_char(r.reserve_start_date, 'YYYY-MM-DD') <= to_char(current_timestamp, 'YYYY-MM-DD')
                  and to_char(r.reserve_end_date, 'YYYY-MM-DD') >= to_char(current_timestamp, 'YYYY-MM-DD'))
             		and r.active_flg = true
						left JOIN item_type as i on i.item_type_id = c.item_type_id
	             		and i.active_flg = true
		             		left JOIN company_master as co on co.company_id = r.company_id
		             		and co.active_flg = true
			             		left JOIN work_type as work on work.work_type_id = r.work_type_id
			             		and work.active_flg = true
			             			left JOIN floor_master as floor on floor.floor_id = r.floor_id
				             		and floor.active_flg = true
           where c.place_id= """  + {placeId} + """
           order by item_other_btx_id ;

        """
      SQL(selectPh).as(otherMasterViewer.*)

    }
  }


  /*その他仮設材登録初期空き情報用 sql文 20180723*/
  def selectOtherMasterReserve(placeId : Int,itemIdList:Seq[Int]): Seq[OtherViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               c.item_other_id
              ,c.item_other_btx_id
              , c.item_type_id
              , i.item_type_name
              , c.note
              , c.item_other_no
              , c.item_other_name
              , c.place_id
              ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
              ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
              ,coalesce(r.company_id, -1) as company_id
              ,coalesce(co.company_name, '無') as company_name
              ,coalesce(work.work_type_id, -1) as work_type_id
              ,coalesce(work.work_type_name, '未予約') as work_type_name
              ,coalesce(floor.floor_name, '無') as reserve_floor_name
              ,coalesce(r.reserve_id, -1) as reserve_id
           from
             		item_other_master as c
            		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
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
              order by item_other_btx_id ;

        """
      SQL(selectPh).as(otherMasterViewer.*)
    }
  }


  /*その他仮設材予約空き情報検索用 sql文 20180723*/
  def getOtherMasterSearch(
                               placeId: Int,
                               itemTypeId: Int,
                               workTypeName: String,
                               reserveStartDate: String,
                               reserveEndDate: String,
                               itemIdList:Seq[Int]
                             ): Seq[OtherViewer] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               c.item_other_id
               ,c.item_other_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_other_no
               , c.item_other_name
               , c.place_id
              ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
              ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '未予約') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_other_master as c
             		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
                and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
             		and r.active_flg = true
                and r.place_id = """ + {placeId} +"""
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
               and r.reserve_start_date <= to_date('${reserveStartDate}', 'YYYY-MM-DD') and r.reserve_start_date >= to_date('${reserveStartDate}', 'YYYY-MM-DD')
               or r.reserve_start_date <= to_date('${reserveEndDate}', 'YYYY-MM-DD') and r.reserve_start_date >= to_date('${reserveEndDate}', 'YYYY-MM-DD')
         """


      // 表示順を設定
      val orderPh =
        """
          order by
            c.item_other_id
        """
      SQL(selectPh + wherePh + orderPh).as(otherMasterViewer.*)
    }
  }


  /*その他仮設材予約空き情報検索用 sql文 20180723*/
  def selectOtherMasterSearch(
                               placeId: Int,
                               itemTypeId: Int,
                               workTypeName: String,
                               reserveStartDate: String,
                               reserveEndDate: String,
                               itemIdList:Seq[Int]
                             ): Seq[OtherViewer] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               c.item_other_id
               ,c.item_other_btx_id
               , c.item_type_id
               , i.item_type_name
               , c.note
               , c.item_other_no
               , c.item_other_name
               , c.place_id
              ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
              ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
               ,coalesce(r.company_id, -1) as company_id
               ,coalesce(co.company_name, '無') as company_name
               ,coalesce(work.work_type_id, -1) as work_type_id
               ,coalesce(work.work_type_name, '未予約') as work_type_name
               ,coalesce(floor.floor_name, '無') as reserve_floor_name
               ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_other_master as c
             		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
                and r.item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
             		and r.active_flg = true
                and r.place_id = """ + {placeId} +"""
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

      val vCount = this.getOtherMasterSearch(placeId, itemTypeId, workTypeName, reserveStartDate,reserveEndDate, itemIdList).length
      //and r.reserve_start_date != to_date('${reserveStartDate}', 'YYYY-MM-DD')
      if(vCount == 0 ){
        wherePh += s"""
            and not r.item_id in
            (select item_id from reserve_table where
                      r.reserve_start_date  between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD')
                      or r.reserve_end_date between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD'))
            """
      }else{
        if(workTypeName == "終日"){
          wherePh += s"""
            and not r.item_id in
            (select item_id from reserve_table where
                      r.reserve_start_date  between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD')
                      or r.reserve_end_date between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD'))
            """
        }else if (workTypeName == ""){
          wherePh += s"""
            and not r.item_id in
            (select item_id from reserve_table where
                      r.reserve_start_date  between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD')
                      or r.reserve_end_date between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD'))
            """
        }else{
          wherePh += s"""
             and not r.item_id in
            (select item_id from reserve_table where (work.work_type_name ='${workTypeName}' or work_type_id = 3)
                and r.reserve_start_date  = to_date('${reserveStartDate}', 'YYYY-MM-DD')
                and r.reserve_end_date = to_date('${reserveEndDate}', 'YYYY-MM-DD')
             )
          """
        }
      }

      wherePh += s""" or coalesce(r.reserve_id, -1) = -1 and c.active_flg = true and c.place_id = ${placeId} """

      // 表示順を設定
      val orderPh =
        """
          order by
            c.item_other_id
        """
      SQL(selectPh + wherePh + orderPh).as(otherMasterViewer.*)
    }
  }

  def reserveItemOther(reserveItemOther: List[ReserveItem]): String = {
    //var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      //reserveItemCar(1).itemTypeId
      reserveItemOther.zipWithIndex.map { case (item, num) =>
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
            //if (!vCheck) {
            //vCheck = true;
            vResult = e + ""
            //}
          }
        }
      }

    }
    vResult
  }


  /*その他仮設材予約空き情報検索用 sql文 20180723*/
  def selectOtherMasterCancelSearch(
                               placeId: Int,
                               itemIdList:Seq[Int],
                               reserveStartDate: String,
                               reserveEndDate: String
                             ): Seq[OtherViewer] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                 c.item_other_id
                ,c.item_other_btx_id
                , c.item_type_id
                , i.item_type_name
                , c.note
                , c.item_other_no
                , c.item_other_name
                , c.place_id
                ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
                ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
                ,coalesce(r.company_id, -1) as company_id
                ,coalesce(co.company_name, '無') as company_name
                ,coalesce(work.work_type_id, -1) as work_type_id
                ,coalesce(work.work_type_name, '未予約') as work_type_name
                ,coalesce(floor.floor_name, '無') as reserve_floor_name
                ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_other_master as c
              		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
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
                and r.reserve_start_date  between to_date('"""  + {reserveStartDate} + """', 'YYYY-MM-DD') and  to_date('"""  + {reserveEndDate} + """', 'YYYY-MM-DD')
                or r.reserve_end_date between to_date('"""  + {reserveStartDate} + """', 'YYYY-MM-DD') and  to_date('"""  + {reserveEndDate} + """', 'YYYY-MM-DD')
                order by item_other_btx_id ;

        """
      SQL(selectPh).as(otherMasterViewer.*)
    }
  }


  /*その他仮設材予約取消用 sql文 20180727*/
  def cancelItemOther(cancelItem: List[CancelItem]): String = {
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
  /*その他仮設材予約取消情報用 sql文 20180726*/
  def selectOtherMasterCancel(placeId : Int,itemIdList:Seq[Int]): Seq[OtherViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                 c.item_other_id
                ,c.item_other_btx_id
                , c.item_type_id
                , i.item_type_name
                , c.note
                , c.item_other_no
                , c.item_other_name
                , c.place_id
                ,coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), '未予約') as reserve_start_date
                ,coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), '未予約') as reserve_end_date
                ,coalesce(r.company_id, -1) as company_id
                ,coalesce(co.company_name, '無') as company_name
                ,coalesce(work.work_type_id, -1) as work_type_id
                ,coalesce(work.work_type_name, '未予約') as work_type_name
                ,coalesce(floor.floor_name, '無') as reserve_floor_name
                ,coalesce(r.reserve_id, -1) as reserve_id
          from
            item_other_master as c
              		LEFT JOIN reserve_table as r on c.item_other_id = r.item_id
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
                order by item_other_btx_id ;

        """
      SQL(selectPh).as(otherMasterViewer.*)
    }
  }

  /**
    * 作業車・立馬情報 仮設材種別チェック用
    * @return
    */
  def selectItemTypeCheck(placeId: Int, itemTypeId: Int): Seq[ItemOther] = {

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
          wherePh += s""" and c.item_type_id = {itemTypeId} """

      val orderPh =
        """
          order by
            c.item_other_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'itemTypeId -> itemTypeId).as(simple.*)
    }
  }

}
