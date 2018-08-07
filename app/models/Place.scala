package models

import javax.inject.Inject
import anorm.SqlParser._
import anorm._
import play.api.db._
import play.api.Logger
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

case class PlaceEnum(
  map: Map[Int,String] = Map[Int,String](
    0 -> "施工前",
    1 -> "施行中",
    9 -> "終了"
  ),
  sortTypeMap: Map[Int,String] = Map[Int,String](
    0 -> "pm.place_id",
    1 -> "pm.updatetime"
  )
)

case class Place(
  placeId: Int,
  placeName: String,
  floorCount: Int = 0,
  status: Int,
  statusName: String = "",
  btxApiUrl: String = "",
  exbTelemetryUrl: String = "",
  gatewayTelemetryUrl: String = "",
  cmsPassword: String = ""
)

case class PlaceEx(
  placeId: Int,
  placeName: String,
  floorCount: Int = 0,
  status: Int,
  statusName: String = "",
  btxApiUrl: String = "",
  exbTelemetryUrl: String = "",
  gatewayTelemetryUrl: String = "",
  cmsPassword: String = "",
  userEmail: String = "",
  userName: String = ""
)

@javax.inject.Singleton
class placeDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 現場情報をID順で取得
    * @return
    */
  def selectPlaceList(placeIdList: Seq[Int] = Seq[Int]()): Seq[Place] = {
    val list = {
      get[Int]("place_id") ~
        get[String]("place_name") ~
        get[Int]("floor_count") ~
        get[Int]("status") ~
        get[String]("btx_api_url")~
        get[String]("exb_telemetry_url")~
        get[String]("gateway_telemetry_url")map {
        case place_id ~ place_name ~ floor_count ~ status
          ~ btx_api_url ~ exb_telemetry_url ~ gateway_telemetry_url =>
          val statusName = PlaceEnum().map(status)
          Place(place_id, place_name, floor_count, status, statusName,
            btx_api_url,exb_telemetry_url,gateway_telemetry_url)
      }
    }
    db.withConnection { implicit connection =>
      var selectPh =
        s"""
          select
              pm.place_id
            , pm.place_name
            , count(f.floor_id) as floor_count
            , pm.status
            , pm.btx_api_url
            , pm.exb_telemetry_url
            , pm.gateway_telemetry_url
            , pm.cms_password
          from
            place_master pm
            left outer join floor_master f
              on pm.place_id = f.place_id
          where
            pm.active_flg = true
          """
      if(placeIdList.isEmpty == false){
        selectPh += s""" and pm.place_id in (${placeIdList.mkString(",")})"""
      }
      val groupPh =
        s"""
              group by
              pm.place_id
            , pm.place_name
            , pm.status
          order by
            pm.place_id
        """
      SQL(selectPh + groupPh).as(list.*)
    }
  }

  /**
    * 現場情報を指定されたソート順で取得
    * @return
    */
  def selectPlaceAll(): Seq[Place] = {
    val list = {
      get[Int]("place_id") ~
        get[String]("place_name") ~
        get[Int]("place_id2") ~
        get[Int]("status") ~
        get[String]("btx_api_url")~
        get[String]("exb_telemetry_url")~
        get[String]("gateway_telemetry_url")map {
        case place_id ~ place_name ~ place_id2 ~ status
          ~ btx_api_url ~ exb_telemetry_url ~ gateway_telemetry_url =>
          val statusName = PlaceEnum().map(status)
          Place(place_id, place_name, place_id2, status, statusName,
            btx_api_url,exb_telemetry_url,gateway_telemetry_url)
      }
    }
    db.withConnection { implicit connection =>

      var selectSQL =
        s"""
          select
             place_id
           , place_name
           , place_id as place_id2
           , status
           , btx_api_url
           , exb_telemetry_url
           , gateway_telemetry_url
         from
           place_master pm
         where
           pm.active_flg = true
           order by place_id

          """
      SQL(selectSQL).as(list.*)
    }
  }

  /**
    * 現場情報を指定されたソート順で取得
    * @return
    */
  def selectPlaceListWithSortType(sortType: Int): Seq[Place] = {
    val list = {
      get[Int]("place_id") ~
        get[String]("place_name") ~
        get[Int]("floor_count") ~
        get[Int]("status") ~
        get[String]("btx_api_url") map {
        case place_id ~ place_name ~ floor_count ~ status ~ btx_api_url =>
          val statusName = PlaceEnum().map(status)
          Place(place_id, place_name, floor_count, status, statusName, btx_api_url)
      }
    }
    db.withConnection { implicit connection =>
      var selectSQL =
        s"""
          select
              pm.place_id
            , pm.place_name
            , count(f.floor_id) as floor_count
            , pm.status
            , pm.btx_api_url
            , pm.cms_password
          from
            place_master pm
            left outer join floor_master f
              on pm.place_id = f.place_id
          where
            pm.active_flg = true
              group by
              pm.place_id
            , pm.place_name
            , pm.status
            order by ${PlaceEnum().sortTypeMap(sortType)}"""
      SQL(selectSQL).as(list.*)
    }
  }

  /**
    * 現場情報を指定されたソート順で取得
    * @return
    */
  def selectPlaceListWithSortTypeEx(sortType: Int): Seq[PlaceEx] = {
    val list = {
      get[Int]("place_id") ~
        get[String]("place_name") ~
        get[Int]("floor_count") ~
        get[Int]("status") ~
        get[String]("btx_api_url") ~
        get[String]("cms_password") ~
        get[String]("user_email") ~
        get[String]("user_name") map {
        case place_id ~ place_name ~ floor_count ~ status ~ btx_api_url ~ cms_password ~ user_email ~ user_name =>
          val statusName = PlaceEnum().map(status)
          PlaceEx(place_id, place_name, floor_count, status, statusName,
            btx_api_url, btx_api_url, btx_api_url, cms_password, user_email, user_name)
      }
    }
    db.withConnection { implicit connection =>
      var selectSQL =
        s"""
          SELECT
              pm.place_id AS place_id,
              pm.place_name AS place_name,
              count(f.floor_id) AS floor_count,
              pm.status AS status,
              pm.btx_api_url AS btx_api_url,
              pm.cms_password AS cms_password,
              COALESCE(u.email, '-') AS user_email,
              COALESCE(u.name, '-') AS user_name
          FROM place_master pm
            LEFT OUTER JOIN floor_master f ON pm.place_id = f.place_id
            LEFT OUTER JOIN user_master u ON pm.place_id = u.place_id AND u.permission = 4
          WHERE pm.active_flg = true
          GROUP BY pm.place_id, pm.place_name, pm.status, u.email, u.name
          ORDER BY ${PlaceEnum().sortTypeMap(sortType)}"""
      SQL(selectSQL).as(list.*)
    }
  }

  /**
    * パスワードで検索し存在を確認する
    * @return
    */
  def isExist(placeId: Int, password: String): Boolean = {
    val count = db.withConnection { implicit connection =>
      val query =
        """
        select
          count(*)
        from
          place_master
        where
          place_id = {placeId}
          and cms_password = {password}
          and active_flg = true
      """

      SQL(query).on('placeId -> placeId, 'password -> password).as(scalar[Long].single)
    }
    count.toInt > 0
  }

  /**
    * 現場の新規登録
    * @return
    */
  def insert(placeName: String): Int = {
    db.withTransaction { implicit connection =>
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "placeName" -> placeName
      )
      // クエリ
      var sql = SQL(
        """
          insert into place_master (place_name)
          values ({placeName})
        """
      ).on(params:_*)

      // SQL実行
      val placeId: Option[Long] = sql.executeInsert()
      // コミット
      connection.commit()

      Logger.debug("現場を新規登録、ID：" + placeId.get.toString)

      placeId.get.toInt
    }
  }

  /**
    * 現場の更新
    * @return
    */
  def updateById(placeId:Int, placeName: String, status: Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update place_master set
              place_name = {placeName}
            , status = {status}
            , updatetime = now()
          where place_id = {placeId} ;
        """).on(
        'placeName -> placeName, 'status -> status, 'placeId -> placeId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""現場情報を更新、ID：" + ${placeId.toString}""")
    }
  }

  /**
    * 現場の更新
    * @return
    */
  def updatePassword(placeId:Int, password: String): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update place_master set
              cms_password = {password}
            , updatetime = now()
          where place_id = {placeId} ;
        """).on(
        'placeId -> placeId, 'password -> password
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""現場パスワードを更新、ID：" + ${placeId.toString}""")
    }
  }

  /**
    * 現場の論理削除
    * @return
    */
  def deleteLogicalById(placeId:Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update place_master set active_flg = false where place_id = {placeId} ;
        """).on(
        'placeId -> placeId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""現場情報を論理削除、ID：" + ${placeId.toString}""")
    }
  }

  /**
    * ユーザの現場IDの更新
    * @return
    */
  def updateCurrentPlaceId(placeId:Int, userId: Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL(
        """
          update user_master set
              current_place_id = {placeId}
          where user_id = {userId} ;
        """).on(
        'placeId -> placeId, 'userId -> userId
      ).executeUpdate()

      // コミット
      connection.commit()

      Logger.debug(s"""現在の現場IDを更新、ID：" + ${userId.toString}""")
    }
  }

}

