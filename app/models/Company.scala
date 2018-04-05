package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

//case class roomPosition(
//  room_id: String,
//  room_name: String,
//  description: String
//)
//object roomPosition {
//
//  implicit val jsonReads: Reads[roomPosition] = (
//      ((JsPath \ "room_id").read[String] | Reads.pure("")) ~
//      ((JsPath \ "room_name").read[String] | Reads.pure(""))~
//      ((JsPath \ "description").read[String] | Reads.pure(""))
//    )(roomPosition.apply _)
//
//  implicit def jsonWrites = Json.writes[roomPosition]
//}



case class Company(
  companyId: Int,
  companyName: String,
  note: String,
  placeId: Int
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
