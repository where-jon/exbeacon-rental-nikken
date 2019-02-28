package models.manage

import anorm.SqlParser._
import anorm._
import javax.inject.Inject
import play.api.Logger
import play.api.db._



case class Company(
  companyId: Int,
  companyName: String,
  note: String,
  placeId: Int
)

/*作業車・立馬管理予約検索用*/
case class ReserveMasterCompanyCheck(
  itemId: Int
  , workTypeId: Int
  , companyId: Int
  , companyName: String
  , reserveStartDate: String
  , reserveEndDate: String
)

@javax.inject.Singleton
class companyDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 業者情報の取得
    * @return
    */
  def selectCompany(placeId: Int, companyName: String = ""): Seq[Company] = {

    val simple = {
      get[Int]("company_id") ~
        get[String]("company_name") ~
        get[String]("note") ~
        get[Int]("place_id") map {
        case company_id ~ company_name ~ note ~ place_id  =>
          Company(company_id, company_name, note, place_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
                c.company_id
              , c.company_name
              , c.note
              , c.place_id
          from
            place_master p
            inner join company_master c
              on p.place_id = c.place_id
              and p.active_flg = true
              and c.active_flg = true
        """

      var wherePh = """ where p.place_id = {placeId} """
      if(companyName.isEmpty == false){
        wherePh += s""" and c.company_name = '${companyName}' """
      }
      val orderPh =
        """
          order by
            c.company_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  val reserveMasterCompanyCheck = {
    get[Int]("item_id") ~
      get[Int]("work_type_id") ~
      get[Int]("company_id") ~
      get[String]("company_name")~
      get[String]("reserve_start_date")~
      get[String]("reserve_end_date") map {
      case item_id ~ work_type_id ~ companyId ~ company_name ~ reserve_start_date ~ reserve_end_date =>
        ReserveMasterCompanyCheck(item_id, work_type_id, companyId, company_name, reserve_start_date, reserve_end_date)
    }
  }
  /*予約状況確認*/
  def selectCarReserveCheck(
                             placeId: Int,
                             companyId: Int
                           ): Seq[ReserveMasterCompanyCheck] = {

    db.withConnection { implicit connection =>
      val selectPh =
        """
         select
            r.item_id
          , r.work_type_id
          , r.company_id
          , c.company_name
          , r.reserve_start_date
          , r.reserve_end_date
         from
            (select
                rr.item_id
              , rr.work_type_id
              , rr.company_id
              , to_char(rr.reserve_start_date, 'YYYYMMDD') as reserve_start_date
              , to_char(rr.reserve_end_date, 'YYYYMMDD') as reserve_end_date
              , rr.place_id
            from
              place_master p
              inner join reserve_table rr
              on p.place_id = rr.place_id
                and p.active_flg = true
                and rr.active_flg = true
            where
                  rr.active_flg = true
              and rr.place_id = """  + {placeId} + """
              and rr.company_id = """  + {companyId} + """) r
            inner join company_master c
            on r.place_id = c.place_id
              and c.active_flg = true
          where
            r.company_id = c.company_id
        """
      // 追加検索条件
      var wherePh = ""
//      wherePh += s""" and r.company_id = {companyId} """

      // 表示順を設定
      val orderPh =
        """
          order by
            r.item_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId, 'companyId -> companyId).as(reserveMasterCompanyCheck.*)
    }
  }

  /**
    * 業者の削除
    * @return
    */
  def deleteById(companyId:Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL("""delete from company_master where company_id = {companyId} ;""").on('companyId -> companyId).executeUpdate()
      // コミット
      connection.commit()

      Logger.debug(s"""業者を削除、ID：" + ${companyId.toString}""")
    }
  }


  /**
    * 業者の新規登録
    * @return
    */
  def insert(companyName: String, note: String, placeId: Int): Int = {
    db.withTransaction { implicit connection =>
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
         "companyName" -> companyName
        ,"note" -> note
        ,"placeId" -> placeId
      )
      // クエリ
      val sql = SQL(
        """
          insert into company_master (company_name, note, place_id) values ({companyName}, {note}, {placeId})
        """)
        .on(params:_*)

      // SQL実行
      val id: Option[Long] = sql.executeInsert()

      // コミット
      connection.commit()

      Logger.debug("業者を新規登録、ID：" + id.get.toString)

      id.get.toInt
    }
  }

  /**
    * 業者の更新
    * @return
    */
  def updateById(companyId:Int, companyName: String, note: String): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update company_master set
              company_name = {companyName}
            , note = {note}
            , updatetime = now()
          where company_id = {companyId} ;
        """).on(
        'companyName -> companyName, 'note -> note, 'companyId -> companyId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""業者情報を更新、ID：" + ${companyId.toString}""")
    }
  }

}

