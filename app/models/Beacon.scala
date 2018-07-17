package models

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
case class beaconPositionData(
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
                               note:String,
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

object beaconPositionData {
  implicit val jsonReads: Reads[beaconPositionData] = (
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
      ((JsPath \ "note").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_car_no").read[String] | Reads.pure("")) ~
      ((JsPath \ "item_car_name").read[String] | Reads.pure("")) ~
      ((JsPath \ "place_id").read[Int])~
      ((JsPath \ "reserve_start_date").read[String] | Reads.pure("")) ~
      (JsPath \ "company_id").read[Int] ~
      ((JsPath \ "company_name").read[String] | Reads.pure(""))~
      (JsPath \ "work_type_id").read[Int] ~
      ((JsPath \ "work_type_name").read[String] | Reads.pure(""))~
      (JsPath \ "reserve_id").read[Int]
    )(beaconPositionData.apply _)
  implicit def jsonWrites = Json.writes[beaconPositionData]
}