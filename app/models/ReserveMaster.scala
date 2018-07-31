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
        get[String]("reserve_start_date")~
        get[String]("reserve_end_date") map {
        case item_id ~ work_type_id ~ reserve_start_date ~ reserve_end_date  =>
          ReserveMasterInfo(item_id, work_type_id, reserve_start_date, reserve_end_date)
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
          from
              reserve_table as r
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
        wherePh += s""" and r.work_type_id  =  ${workTypeId} """
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
        get[String]("reserve_start_date")~
        get[String]("reserve_end_date") map {
        case item_id ~ work_type_id ~ reserve_start_date ~ reserve_end_date  =>
          ReserveMasterInfo(item_id, work_type_id, reserve_start_date, reserve_end_date)
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
          from
              reserve_table as r
          where
            r.active_flg = true
        """


      var wherePh = ""

      wherePh += s""" and r.place_id = ${placeId} """

      if(!reserveStartDate.isEmpty && !reserveEndDate.isEmpty){
        wherePh += s""" and reserve_start_date between to_date('${reserveStartDate}', 'YYYY-MM-DD')  and  to_date('${reserveEndDate}', 'YYYY-MM-DD')
                        or reserve_end_date between to_date('${reserveStartDate}', 'YYYY-MM-DD') and  to_date('${reserveEndDate}', 'YYYY-MM-DD') """
      }

      if(!itemIdList.isEmpty){
        wherePh += s""" and r.item_id in ( ${itemIdList.mkString(",")} ) """
      }
      if(!itemTypeIdList.isEmpty){
        wherePh += s""" and r.item_type_id in ( ${itemTypeIdList.mkString(",")} ) """
      }

      if(workTypeId!=null){
        wherePh += s""" and r.work_type_id  =  ${workTypeId} """
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



}
