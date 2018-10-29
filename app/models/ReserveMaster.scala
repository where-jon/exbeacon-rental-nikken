package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._



case class WorkTypeCount(
item_count: Int,
item_id: Int
)

case class ReserveMasterInfo(
itemId: Int
, workTypeId: Int
, txId: Int
, workTypeName: String
, reserveStartDate: String
, reserveEndDate: String
)

@javax.inject.Singleton
class ReserveMasterDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 作業車・立馬予約情報の取得
    * @return
    */
  def selectCarReserve(
  placeId: Int
  , itemIdList:Seq[Int] = Seq[Int]()
  , itemTypeIdList:Seq[Int] = Seq[Int]()
  , workTypeId: Int
  , reserveStartDate:String = ""
  , reserveEndDate:String = ""
  ): Seq[ReserveMasterInfo] = {

    val simple = {
      get[Int]("item_id") ~
        get[Int]("work_type_id") ~
        get[Int]("item_tx") ~
        get[String]("work_type_name") ~
        get[String]("reserve_start_date")~
        get[String]("reserve_end_date") map {
        case item_id ~ work_type_id ~ item_tx ~ work_type_name ~ reserve_start_date ~ reserve_end_date  =>
          ReserveMasterInfo(item_id, work_type_id,item_tx,work_type_name, reserve_start_date, reserve_end_date)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.item_id
            , r.work_type_id
            , car.item_car_btx_id as item_tx
            , w.work_type_name
            , to_char(r.reserve_start_date, 'YYYYMMDD') as reserve_start_date
            , to_char(r.reserve_end_date, 'YYYYMMDD') as reserve_end_date
          from
              reserve_table as r
              left JOIN item_car_master as car on car.item_car_id = r.item_id
              left JOIN work_type as w on w.work_type_id = r.work_type_id
          where
            r.active_flg = true
        """


      var wherePh = ""

      wherePh += s""" and r.place_id = ${placeId} """

      if(!reserveStartDate.isEmpty && !reserveEndDate.isEmpty){
        wherePh += s""" and reserve_start_date = to_date('${reserveStartDate}', 'YYYY-MM-DD')
                        and reserve_end_date = to_date('${reserveEndDate}', 'YYYY-MM-DD') """
      }

      if(!itemIdList.isEmpty){
        wherePh += s""" and r.item_id in ( ${itemIdList.mkString(",")} ) """
      }
      if(!itemTypeIdList.isEmpty){
        wherePh += s""" and r.item_type_id in ( ${itemTypeIdList.mkString(",")} ) """
      }

      if(workTypeId!=null){
        if(workTypeId == 3){  // 終日の場合
          wherePh += s""" and r.work_type_id in  (1,2,3) """
        }else {
          wherePh += s""" and (r.work_type_id  =  ${workTypeId} or r.work_type_id =  3) """
        }
      }

      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh+ wherePh + orderPh).as(simple.*)
    }
  }

  /**
    * その他仮設材予約情報の取得
    * @return
    */
  def selectOtherReserve(
      placeId: Int
      , itemIdList:Seq[Int] = Seq[Int]()
      , itemTypeIdList:Seq[Int] = Seq[Int]()
      , workTypeId: Int
      , reserveStartDate:String = ""
      , reserveEndDate:String = ""
    ): Seq[ReserveMasterInfo] = {

    val simple = {
      get[Int]("item_id") ~
        get[Int]("work_type_id") ~
        get[Int]("item_tx") ~
        get[String]("work_type_name") ~
        get[String]("reserve_start_date")~
        get[String]("reserve_end_date") map {
        case item_id ~ work_type_id ~ item_tx ~ work_type_name ~ reserve_start_date ~ reserve_end_date  =>
          ReserveMasterInfo(item_id, work_type_id,item_tx ,work_type_name,  reserve_start_date, reserve_end_date)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.item_id
            , r.work_type_id
            , to_char(r.reserve_start_date, 'YYYYMMDD') as reserve_start_date
            , to_char(r.reserve_end_date, 'YYYYMMDD') as reserve_end_date
              , other.item_other_btx_id as item_tx
              , w.work_type_name
          from
              reserve_table as r
              left JOIN item_other_master as other on other.item_other_id = r.item_id
              left JOIN work_type as w on w.work_type_id = r.work_type_id
          where
            r.active_flg = true
        """


      var wherePh = ""

      wherePh += s""" and r.place_id = ${placeId} """

      if(!reserveStartDate.isEmpty && !reserveEndDate.isEmpty){
        wherePh += s""" and (reserve_start_date between to_date('${reserveStartDate}', 'YYYY-MM-DD')  and  to_date('${reserveEndDate}', 'YYYY-MM-DD')
                        or reserve_end_date between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD') )"""
      }

      if(!itemIdList.isEmpty){
        wherePh += s""" and r.item_id in ( ${itemIdList.mkString(",")} ) """
      }
      if(!itemTypeIdList.isEmpty){
        wherePh += s""" and r.item_type_id in ( ${itemTypeIdList.mkString(",")} ) """
      }

      if(workTypeId!=null){
        if(workTypeId == 3){  // 終日の場合
          wherePh += s""" and r.work_type_id in  (1,2,3) """
        }else{
          wherePh += s""" and (r.work_type_id  =  ${workTypeId} or r.work_type_id =  3) """
        }

      }

      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh+ wherePh + orderPh).as(simple.*)
    }
  }


  /*作業車・立馬判定用 sql文 20180727*/
  val workTypeCount = {
    get[Int]("item_count") ~
      get[Int]("item_id")map {
      case item_count ~ item_id =>
        WorkTypeCount(item_count, item_id)
    }
  }
  def getCarMasterWorkTypeCount(
                                 placeId: Int,
                                 reserveDate: String,
                                 itemIdList:Seq[Int]
                               ): Seq[WorkTypeCount] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """

    select count(item_id)as item_count, item_id
    from reserve_table
    where
    item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
    and active_flg = true
    and place_id =  """  + {placeId} + """
    and reserve_start_date = to_date('
    """ +  {reserveDate} + """
    ', 'YYYY-MM-DD')
    and (work_type_id = 1 or work_type_id = 2)
    GROUP BY item_id
        """

      // 表示順を設定
      val orderPh =
        """
          order by
            item_id
        """
      SQL(selectPh + orderPh).as(workTypeCount.*)
    }
  }


  /*判定用 sql文 20180727*/
  def getOtherMasterWorkTypeCount(
                                   placeId: Int,
                                   reserveStartDate: String,
                                   reserveEndDate: String,
                                   itemIdList:Seq[Int]
                                 ): Seq[WorkTypeCount] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """

  select count(item_id)as item_count, item_id
    from reserve_table
    where
    item_type_id in ( """ + {itemIdList.mkString(",")} +""" )
    and active_flg = true
    and place_id =  """  + {placeId} + """
    and
     reserve_start_date  between to_date('
      """ + {reserveStartDate} +"""
     ', 'YYYY-MM-DD') and  to_date('
       """ + {reserveEndDate} +"""
     ', 'YYYY-MM-DD')
    or
     reserve_end_date  between to_date('
      """ + {reserveStartDate} +"""
     ', 'YYYY-MM-DD') and  to_date('
       """ + {reserveEndDate} +"""
     ', 'YYYY-MM-DD')
    and (work_type_id = 1 or work_type_id = 2)
    GROUP BY item_id
        """

      // 表示順を設定
      val orderPh =
        """
          order by
            item_id
        """
      SQL(selectPh + orderPh).as(workTypeCount.*)
    }
  }

  /**
    * 仮設材種別チェック用
    * @return
    */
  def selectReserveItemTypeCheck(
                        placeId: Int
                        , itemTypeId: Int
                      ): Seq[ReserveMasterInfo] = {

    val simple = {
      get[Int]("item_id") ~
        get[Int]("work_type_id") ~
        get[Int]("item_tx") ~
        get[String]("work_type_name") ~
        get[String]("reserve_start_date")~
        get[String]("reserve_end_date") map {
        case item_id ~ work_type_id ~ item_tx ~ work_type_name ~ reserve_start_date ~ reserve_end_date  =>
          ReserveMasterInfo(item_id, work_type_id, item_tx, work_type_name,reserve_start_date, reserve_end_date)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              r.item_id
            , r.work_type_id
            , 0 as item_tx
            , w.work_type_name
            , to_char(r.reserve_start_date, 'YYYYMMDD') as reserve_start_date
            , to_char(r.reserve_end_date, 'YYYYMMDD') as reserve_end_date
          from
              reserve_table as r
              left JOIN item_car_master as car on car.item_car_id = r.item_id
              left JOIN item_other_master as other on other.item_other_id = r.item_id
              left JOIN work_type as w on w.work_type_id = r.work_type_id
          where
            r.active_flg = true
        """
      var wherePh = ""
      wherePh += s""" and r.place_id = {placeId} """
      wherePh += s""" and r.item_type_id = {itemTypeId} """

      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh+ wherePh + orderPh).on('placeId -> placeId, 'itemTypeId -> itemTypeId).as(simple.*)
    }
  }

  /**
    * 仮設材ログ削除バッチ用
    * @return
    */
  def batchDelete(placeId: Int, deltedate: String): Unit = {
    db.withTransaction { implicit connection =>

      // 作業車の削除
      SQL(
        """
           delete from reserve_table
           where place_id = {placeId}
           and updatetime < TO_TIMESTAMP({deltedate}, 'YYYY/MM/DD HH24:MI:SS');
        """
          .stripMargin).on('placeId -> placeId, 'deltedate -> deltedate).executeUpdate()

      // コミット
      connection.commit()
    }
  }
}

