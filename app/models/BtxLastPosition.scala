package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm.{~, _}
//import play.api.Logger
import play.api.db._
import play.api.libs.functional.syntax._


//case class BtxNameEnum(
//    kindNameMap: Map[Int,String] = Map[Int,String](1 -> "作業車", 2 -> "作業車鍵", 3 -> "仮設材")
//  , notePrefixMap: Map[Int,String] = Map[Int,String](1 -> "作業車番号：", 2 -> "作業車番号：", 3 -> "仮設材管理No.")
//)

case class BtxLastPosition(
  btxId: Int,
  placeId: Int,
  floorId: Int
)


@javax.inject.Singleton
class btxLastPositionDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * BTXマスタ情報の取得
    * @return
    */
  def find(placeId:Int, btxIdList: Seq[Int] = Seq[Int]()): Seq[BtxLastPosition] = {

    val simple = {
      get[Int]("btx_id") ~
      get[Int]("place_id") ~
        get[Int]("floor_id")  map {
        case btx_id ~ place_id ~ floor_id  =>
          BtxLastPosition(btx_id , place_id ,floor_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              b.btx_id
            , b.place_id
            , b.floor_id
          from
            btx_last_position b
          where
            1 = 1
        """
      var wherePh = """ and b.place_id = {placeId} """
      if(btxIdList.isEmpty == false){
        wherePh += s""" and b.btx_id in (${btxIdList.mkString(",")}) """
      }
      val orderPh =
        """
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  def update(inputList: Seq[BtxLastPosition]) = {
    db.withTransaction { implicit connection =>

      inputList.map{ param =>
        val records = find(param.placeId, Seq[Int](param.btxId))
        if(records.nonEmpty){
          // 更新
          SQL(
            """
              update btx_last_position
              set floor_id = {floorId}, updatetime = now()
              where btx_id = {btxId}
              and place_id = {placeId}
            """)
            .on('btxId -> param.btxId, 'placeId -> param.placeId, 'floorId -> param.floorId)
            .executeUpdate()

        }else{
          // 登録
          SQL(
            """
               insert into btx_last_position(btx_id, place_id, floor_id)
               values ( {btxId}, {placeId}, {floorId} );
            """)
            .on('btxId -> param.btxId, 'placeId -> param.placeId, 'floorId -> param.floorId)
            .executeInsert()
        }
      }

      // コミット
      connection.commit()
    }
  }

}

