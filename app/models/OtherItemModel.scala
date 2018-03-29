package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._


//  仮設材情報（サマリー用）
case class OtherItemSummeryInfo(
    floorId: Int
  , floorName: String
  , count: Int
)
// 仮設材情報
case class OtherItemInfo(
     itemNo: String = ""
   , itemKindId: Int = 0
   , itemKindName: String = ""
   , item_btx_id: Int = 0
)

object OtherItemInfo {
  implicit def jsonWrites = Json.writes[OtherItemInfo]
}

@javax.inject.Singleton
class otherItemDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * 仮設材情報の取得
    * @return
    */
  def selectItemInfo(placeId:Int): Seq[OtherItemInfo] = {

    val simple = {
      get[String]("item_no") ~
        get[Int]("item_kind_id") ~
        get[String]("item_kind_name") ~
        get[Int]("item_btx_id")  map {
        case item_no ~ item_kind_id ~ item_kind_name ~ item_btx_id  =>
          OtherItemInfo(item_no, item_kind_id, item_kind_name, item_btx_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              it.item_no
            , it.item_kind_id
            , ik.item_kind_name
            , it.item_btx_id
          from
            item_table it
            inner join item_kind_master ik
              on it.item_kind_id = ik.item_kind_id
          where
            ik.place_id = {placeId}
            and ik.active_flg = true
            and it.active_flg = true
        """
      val orderPh =
        """
          order by
            it.item_btx_id
        """
      SQL(selectPh  + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  /**
    * 仮設材情報の取得
    * @return
    */
  def selectItemMap(placeId:Int): Map[Int,String] = {

    var resultMap = Map[Int,String]()

    val simple = {
        get[Int]("item_kind_id") ~
        get[String]("item_kind_name") map {
        case item_kind_id ~ item_kind_name   =>
          (item_kind_id, item_kind_name)
      }
    }

    val ret = db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              ik.item_kind_id
            , ik.item_kind_name
          from
            item_kind_master ik
          where
            ik.place_id = {placeId}
            and ik.active_flg = true
        """
      val orderPh =
        """
          order by
            ik.item_kind_id
        """
      SQL(selectPh  + orderPh).on('placeId -> placeId).as(simple.*)
    }

    ret.foreach(d =>{
      resultMap = resultMap + (d._1 -> d._2)
    })
    resultMap
  }

}

