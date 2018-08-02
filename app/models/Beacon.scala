package models

import javax.inject.Inject

import anorm.SqlParser.get
import anorm.{SQL, ~}
import play.api.db.DBApi
import play.api.libs.json._
//import play.api.Logger
import play.api.libs.functional.syntax._

/**
  * 近傍ビーコン情報
  *
  * @param device_id   BeaconTXのデバイスID
  * @param pos_id      位置を表すID
  * @param rssi        電波強度
  * @param timestamp   当日最後に検出した時刻
  */
case class nearestBeaconPosition(
    device_id: Int,
    pos_id: Int,
    rssi: Double,
    timestamp: Long
)

object nearestBeaconPosition {
  implicit val jsonReads: Reads[nearestBeaconPosition] = (
    ((JsPath \ "device_id").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "place_id").read[Int] | Reads.pure(-1)) ~
      ((JsPath \ "rssi").read[Double] | Reads.pure(0.0)) ~
      ((JsPath \ "timestamp").read[Long] | Reads.pure(0L))
    )(nearestBeaconPosition.apply _)

  implicit val jsonWrites: Writes[nearestBeaconPosition] = (
    (JsPath \ "device_id").write[Int] ~
      (JsPath \ "place_id").write[Int] ~
      (JsPath \ "rssi").write[Double] ~
      (JsPath \ "timestamp").write[Long]
    )(unlift(nearestBeaconPosition.unapply))
}

/**
  * EXCloud測位APIから取得するデータモデル
  *
  * @param btx_id        BeaconTXのID
  * @param btx_type      Beaconタイプ
  * @param pos_id        位置を表すID
  * @param phase         電波強度算出時の検出タイミング
  * @param power_level   BeaconTXの電池残量
  * @param nearest       近傍ビーコン情報
  * @param updatetime    当日最後に検出した時刻
  */
case class beaconPosition(
  btx_id: Int,
  device_id: Int,
  btx_type: Int,
  pos_id: Int,
  phase: Int,
  power_level: Int,
  nearest: Seq[nearestBeaconPosition],
  updatetime: String
) {
  def copy(
            btx_id: Int = this.btx_id,
            device_id: Int = this.device_id,
            btx_type: Int = this.btx_type,
            pos_id: Int = this.pos_id,
            phase: Int = this.phase,
            power_level: Int = this.power_level,
            updatetime: String = this.updatetime
          ): beaconPosition = {
    val nearestSeq = this.nearest.map { n =>
      n.copy()
    }
    beaconPosition(btx_id, device_id, btx_type, pos_id, phase, power_level, nearestSeq, updatetime)
  }
}

object beaconPosition {
  implicit val jsonReads: Reads[beaconPosition] = (
    (JsPath \ "btx_id").read[Int] ~
      ((JsPath \ "device_id").read[Int] | Reads.pure(-1)) ~
      ((JsPath \ "btx_type").read[Int] | Reads.pure(0)) ~
      (JsPath \ "pos_id").read[Int] ~
      ((JsPath \ "phase").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "power_level").read[Int] | Reads.pure(-1)) ~
      ((JsPath \ "nearest").read[Seq[nearestBeaconPosition]] | Reads.pure(Seq.empty[nearestBeaconPosition])) ~
      ((JsPath \ "updatetime").read[String] | Reads.pure(""))
    )(beaconPosition.apply _)

  implicit def jsonWrites = Json.writes[beaconPosition]
}


/**
  * 測位APIの結果をフロントエンド側に返却するためのデータモデル
  *
  * @param btx_id        BeaconTXのID
  * @param pos_id        位置を表すID
  * @param phase         電波強度算出時の検出タイミング
  * @param power_level   BeaconTXの電池残量
  * @param updatetime    当日最後に検出した時刻
  */
case class itemCarBeaconPositionData(
  cur_exb_name: String,
  cur_pos_name: String,
  btx_id: Int,
  pos_id: Int,
  phase: Int,
  power_level: Int,
  updatetime: String,
  item_car_id: Int,
  item_car_btx_id: Int,
  item_car_key_btx_id: Int,
  item_type_id: Int,
  item_type_name:String,
  reserve_floor_name:String,
  item_car_no: String,
  item_car_name:String,
  place_id: Int,
  reserve_start_date:String,
  company_id: Int,
  company_name: String,
  work_type_id: Int,
  work_type_name: String,
  reserve_id: Int

 )

object itemCarBeaconPositionData {
  implicit val jsonReads: Reads[itemCarBeaconPositionData] = (
    ((JsPath \ "cur_exb_name").read[String] | Reads.pure(""))~
    ((JsPath \ "cur_pos_name ").read[String] | Reads.pure(""))~
    (JsPath \ "btx_id").read[Int] ~
    (JsPath \ "pos_id").read[Int] ~
    ((JsPath \ "phase").read[Int] | Reads.pure(0)) ~
    (JsPath \ "power_level").read[Int] ~
    ((JsPath \ "updatetime").read[String] | Reads.pure(""))~
    (JsPath \ "item_car_id").read[Int] ~
    (JsPath \ "item_car_btx_id").read[Int] ~
    (JsPath \ "item_car_key_btx_id").read[Int]~
    (JsPath \ "item_type_id").read[Int] ~
    ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
    ((JsPath \ "reserve_floor_name").read[String] | Reads.pure("")) ~
    ((JsPath \ "item_car_no").read[String] | Reads.pure("")) ~
    ((JsPath \ "item_car_name").read[String] | Reads.pure("")) ~
    ((JsPath \ "place_id").read[Int])~
    ((JsPath \ "reserve_start_date").read[String] | Reads.pure("")) ~
    (JsPath \ "company_id").read[Int] ~
    ((JsPath \ "company_name").read[String] | Reads.pure(""))~
    (JsPath \ "work_type_id").read[Int] ~
    ((JsPath \ "work_type_name").read[String] | Reads.pure(""))~
    (JsPath \ "reserve_id").read[Int]
    )(itemCarBeaconPositionData.apply _)
  implicit def jsonWrites = Json.writes[itemCarBeaconPositionData]
}


/**
  * 測位APIの結果をフロントエンド側に返却するためのデータモデル
  *
  * @param btx_id        BeaconTXのID
  * @param pos_id        位置を表すID
  * @param phase         電波強度算出時の検出タイミング
  * @param power_level   BeaconTXの電池残量
  * @param updatetime    当日最後に検出した時刻
  */
case class itemOtherBeaconPositionData(
  cur_exb_name: String,
  cur_pos_name: String,
  btx_id: Int,
  pos_id: Int,
  phase: Int,
  power_level: Int,
  updatetime: String,
  item_other_id: Int,
  item_other_btx_id: Int,
  item_type_id: Int,
  item_type_name:String,
  reserve_floor_name:String,
  item_other_no: String,
  item_other_name:String,
  place_id: Int,
  reserve_start_date:String,
  reserve_end_date:String,
  company_id: Int,
  company_name: String,
  work_type_id: Int,
  work_type_name: String,
  reserve_id: Int

)

object itemOtherBeaconPositionData {
  implicit val jsonReads: Reads[itemOtherBeaconPositionData] = (
    ((JsPath \ "cur_exb_name").read[String] | Reads.pure(""))~
      ((JsPath \ "cur_pos_name ").read[String] | Reads.pure(""))~
      (JsPath \ "btx_id").read[Int] ~
      (JsPath \ "pos_id").read[Int] ~
      ((JsPath \ "phase").read[Int] | Reads.pure(0)) ~
      (JsPath \ "power_level").read[Int] ~
      ((JsPath \ "updatetime").read[String] | Reads.pure(""))~
      (JsPath \ "item_other_id").read[Int] ~
      (JsPath \ "item_other_btx_id").read[Int] ~
      (JsPath \ "item_type_id").read[Int] ~
      ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "reserve_floor_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_other_no").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_other_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "place_id").read[Int])~
      ((JsPath \ "reserve_start_date").read[String] | Reads.pure("")) ~
      ((JsPath \ "reserve_end_date").read[String] | Reads.pure("")) ~
      (JsPath \ "company_id").read[Int] ~
      ((JsPath \ "company_name").read[String] | Reads.pure(""))~
      (JsPath \ "work_type_id").read[Int] ~
      ((JsPath \ "work_type_name").read[String] | Reads.pure(""))~
      (JsPath \ "reserve_id").read[Int]
    )(itemOtherBeaconPositionData.apply _)
  implicit def jsonWrites = Json.writes[itemOtherBeaconPositionData]
}



/**
  * 測位APIの結果をフロントエンド側に返却するためのデータモデル
  *
  * @param btx_id        BeaconTXのID
  * @param pos_id        位置を表すID
  * @param power_level   BeaconTXの電池残量
  * @param updatetime    当日最後に検出した時刻
  */
case class itemBeaconPositionData(
  cur_exb_name: String,
  cur_pos_name: String,
  btx_id: Int,
  pos_id: Int,
  power_level: Int,
  updatetime: String,
  item_id: Int,
  item_btx_id: Int,
  item_key_btx: Int,
  item_type_id: Int,
  item_type_name:String,
  item_no: String,
  item_name:String,
  place_id: Int,
  item_type_icon_color:String,
  item_type_text_color: String,
  company_name: String,
  work_type_name: String,
  reserve_floor_name: String,
  reserve_id: Int,
  reserve_start_date:String,
  reserve_end_date: String
)

object itemBeaconPositionData {
  implicit val jsonReads: Reads[itemBeaconPositionData] = (
    ((JsPath \ "cur_exb_name").read[String] | Reads.pure(""))~
    ((JsPath \ "cur_pos_name ").read[String] | Reads.pure(""))~
    (JsPath \ "btx_id").read[Int] ~
    (JsPath \ "pos_id").read[Int] ~
    (JsPath \ "power_level").read[Int] ~
    ((JsPath \ "updatetime").read[String] | Reads.pure(""))~
    (JsPath \ "item_id").read[Int] ~
    (JsPath \ "item_btx_id").read[Int] ~
    (JsPath \ "item_key_btx").read[Int] ~
    (JsPath \ "item_type_id").read[Int] ~
    ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
    ((JsPath \ "item_no").read[String] | Reads.pure("")) ~
    ((JsPath \ "item_name").read[String] | Reads.pure("")) ~
    ((JsPath \ "place_id").read[Int])~
    ((JsPath \ "item_type_icon_color").read[String] | Reads.pure("")) ~
    ((JsPath \ "item_type_text_color").read[String] | Reads.pure("")) ~
    ((JsPath \ "company_name").read[String] | Reads.pure(""))~
    ((JsPath \ "work_type_name").read[String] | Reads.pure(""))~
    ((JsPath \ "reserve_floor_name").read[String] | Reads.pure(""))~
    (JsPath \ "reserve_id").read[Int]~
    ((JsPath \ "reserve_start_date").read[String] | Reads.pure(""))~
    ((JsPath \ "reserve_end_date").read[String] | Reads.pure(""))
    )(itemBeaconPositionData.apply _)
  implicit def jsonWrites = Json.writes[itemBeaconPositionData]
}



/**
  * ItemLog用測位APIの結果をフロントエンド側に返却するためのデータモデル
  *
  * @param btx_id        BeaconTXのID
  * @param pos_id        位置を表すID
  * @param work_flg   BeaconTXの電池残量
  * @param updatetime    当日最後に検出した時刻
  */
case class itemLogPositionData(
  cur_exb_name: String,
  cur_pos_name: String,
  btx_id: Int,
  pos_id: Int,
  work_flg: Boolean,
  updatetime: String,
  item_id: Int,
  item_btx_id: Int,
  item_key_btx: Int,
  item_type_id: Int,
  item_type_name:String,
  item_no: String,
  item_name:String,
  place_id: Int,
  item_type_icon_color:String,
  item_type_text_color: String,
  company_name: String,
  work_type_name: String,
  reserve_floor_name: String,
  reserve_id: Int,
  reserve_start_date:String,
  reserve_end_date: String
)

object itemLogPositionData {
  implicit val jsonReads: Reads[itemLogPositionData] = (
    ((JsPath \ "cur_exb_name").read[String] | Reads.pure(""))~
      ((JsPath \ "cur_pos_name ").read[String] | Reads.pure(""))~
      (JsPath \ "btx_id").read[Int] ~
      (JsPath \ "pos_id").read[Int] ~
      (JsPath \ "work_flg").read[Boolean] ~
      ((JsPath \ "updatetime").read[String] | Reads.pure(""))~
      (JsPath \ "item_id").read[Int] ~
      (JsPath \ "item_btx_id").read[Int] ~
      (JsPath \ "item_key_btx").read[Int] ~
      (JsPath \ "item_type_id").read[Int] ~
      ((JsPath \ "item_type_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_no").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "place_id").read[Int])~
      ((JsPath \ "item_type_icon_color").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_type_text_color").read[String] | Reads.pure("")) ~
      ((JsPath \ "company_name").read[String] | Reads.pure(""))~
      ((JsPath \ "work_type_name").read[String] | Reads.pure(""))~
      ((JsPath \ "reserve_floor_name").read[String] | Reads.pure(""))~
      (JsPath \ "reserve_id").read[Int]~
      ((JsPath \ "reserve_start_date").read[String] | Reads.pure(""))~
      ((JsPath \ "reserve_end_date").read[String] | Reads.pure(""))
    )(itemLogPositionData.apply _)
  implicit def jsonWrites = Json.writes[itemLogPositionData]
}

case class BeaconViewer(
   item_id: Int,
   item_btx_id: Int,
   item_key_btx: Int,
   item_type_id: Int,
   item_type_name:String,
   item_type_icon_color:String,
   item_type_text_color: String,
   item_type_row_color:String,
   note:String,
   item_no: String,
   item_name:String,
   place_id: Int,
   reserve_start_date:String,
   reserve_end_date: String,
   company_id: Int,
   company_name: String,
   work_type_id: Int,
   work_type_name: String,
   reserve_floor_name: String,
   reserve_id: Int
)


/**
  * EXCloud測位APIから取得するデータモデル
  *
  * @param num          GwのID
  * @param deviceid     位置を表すID
  * @param updated      電波強度算出時の検出タイミング
  * @param timestamp    当日最後に検出した時刻
  */
case class gateWayState(
                         num: Int,
                         deviceid: Int,
                         updated: Long,
                         timestamp: Long
                       ) {
  def copy(
            num: Int = this.num,
            deviceid: Int = this.deviceid,
            updated: Long = this.updated,
            timestamp: Long = this.timestamp
          ): gateWayState = {
    gateWayState(num, deviceid, updated, timestamp)
  }
}

object gateWayState {
  implicit val jsonReads: Reads[gateWayState] = (
    (JsPath \ "num").read[Int] ~
      (JsPath \ "deviceid").read[Int] ~
      ((JsPath \ "updated").read[Long] | Reads.pure(0L)) ~
      ((JsPath \ "timestamp").read[Long] | Reads.pure(0L))
    )(gateWayState.apply _)

  implicit def jsonWrites = Json.writes[gateWayState]
}

@javax.inject.Singleton
class beaconDAO @Inject()(dbapi: DBApi) {
  private val db = dbapi.database("default")


  val beaconDaoViewer = {
    get[Int]("item_id") ~
      get[Int]("item_btx_id") ~
      get[Int]("item_key_btx") ~
      get[Int]("item_type_id") ~
      get[String]("item_type_name") ~
      get[String]("item_type_icon_color") ~
      get[String]("item_type_text_color") ~
      get[String]("item_type_row_color") ~
      get[String]("note") ~
      get[String]("item_no") ~
      get[String]("item_name") ~
      get[Int]("place_id") ~
      get[String]("reserve_start_date") ~
      get[String]("reserve_end_date") ~
      get[Int]("company_id") ~
      get[String]("company_name") ~
      get[Int]("work_type_id") ~
      get[String]("work_type_name") ~
      get[String]("reserve_floor_name") ~
      get[Int]("reserve_id")map {
      case item_id ~ item_btx_id ~ item_key_btx ~ item_type_id ~ item_type_name ~item_type_icon_color ~item_type_text_color ~ item_type_row_color ~
        note ~ item_no ~item_name ~place_id ~reserve_start_date ~ reserve_end_date ~ company_id ~company_name ~work_type_id ~
        work_type_name ~reserve_floor_name ~ reserve_id  =>
        BeaconViewer(item_id, item_btx_id, item_key_btx, item_type_id, item_type_name,item_type_icon_color,item_type_text_color,item_type_row_color,
          note, item_no, item_name, place_id, reserve_start_date, reserve_end_date,company_id,company_name,work_type_id,
          work_type_name,reserve_floor_name,reserve_id)
    }
  }

  /*現場状況画面用 sql文 20180723*/
  def selectBeaconViewer(placeId: Int): Seq[BeaconViewer] = {
    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
               c.item_id,
               c.item_btx_id,
               c.item_key_btx,
               c.item_type_id,
               i.item_type_name,
               i.item_type_icon_color,
               i.item_type_text_color,
               i.item_type_row_color,
               c.note,
               c.item_no,
               c.item_name,
               c.place_id,
               coalesce(to_char(r.reserve_start_date, 'YYYY-MM-DD'), 'date') as reserve_start_date,
               coalesce(to_char(r.reserve_end_date, 'YYYY-MM-DD'), 'date') as reserve_end_date,
               coalesce(r.company_id, -1) as company_id,
               coalesce(co.company_name, '無') as company_name,
               coalesce(work.work_type_id, -1) as work_type_id,
               coalesce(work.work_type_name, '無') as work_type_name,
               coalesce(floor.floor_name, '無') as reserve_floor_name,
               coalesce(r.reserve_id, -1) as reserve_id
               from
                  (SELECT
                  item_type_id ,
                  item_car_id as item_id,
                  item_car_btx_id as item_btx_id,
                  item_car_key_btx_id as item_key_btx,
                  note as note,
                  item_car_no as item_no,
                  item_car_name as item_name,
                  place_id,
                  active_flg
                  FROM item_car_master as a
                  UNION all
                  SELECT item_type_id,
                  item_other_id  as item_id ,
                  item_other_btx_id as item_btx_id,
                  -1 as item_key_btx,
                  note as note,
                  item_other_no as item_no,
                  item_other_name as item_name,
                  place_id,
                  active_flg
                  FROM item_other_master as b
                  ) as c
                left JOIN reserve_table as r on c.item_id = r.item_id and c.item_type_id = r.item_type_id
                and r.active_flg = true
                left JOIN item_type as i on i.item_type_id = c.item_type_id
                and i.active_flg = true
                left JOIN company_master as co on co.company_id = r.company_id
                and co.active_flg = true
                left JOIN work_type as work on work.work_type_id = r.work_type_id
                and work.active_flg = true
                left JOIN floor_master as floor on floor.floor_id = r.floor_id
                and floor.active_flg = true
                  where c.place_id =  """  + {placeId} + """
                  and c.active_flg = true
                  order by c.item_btx_id

        """
      SQL(selectPh).as(beaconDaoViewer.*)
    }
  }
}