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
  map: Map[Int,String] = Map[Int,String](0 -> "施工前", 1 -> "施行中", 9 -> "終了"),
  sortTypeMap: Map[Int,String] = Map[Int,String](0 -> "pm.place_id", 1 -> "pm.updatetime")
)

case class Place(
  placeId: Int,
  placeName: String,
  floorCount: Int = 0,
  status: Int,
  statusName: String = "",
  btxApiUrl: String = "",
  cmsPassword: String = ""
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
        get[String]("btx_api_url") map {
        case place_id ~ place_name ~ floor_count ~ status ~ btx_api_url =>
          val statusName = PlaceEnum().map(status)
          Place(place_id, place_name, floor_count, status, statusName, btx_api_url)
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
    * 現場の新規登録
    * @return
    */
  def insert(placeName: String, btxApiUrl: String): Int = {
    db.withTransaction { implicit connection =>
      // パラメータのセット
      val params: Seq[NamedParameter] = Seq(
        "placeName" -> placeName,
        "btxApiUrl" -> btxApiUrl
      )
      // クエリ
      var sql = SQL(
        """
          insert into place_master (place_name, btx_api_url)
          values ({placeName}, {btxApiUrl})
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
        'password -> password
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

