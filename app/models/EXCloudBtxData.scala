package models

//import play.api.Logger

import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._


case class nearestData(
  device_id: Int,
  timestamp: Int,
  rssi: Double,
  place_id: Int
)

object nearestData {

  implicit val jsonReads: Reads[nearestData] = (
      ((JsPath \ "device_id").read[Int] | Reads.pure(0))~
      ((JsPath \ "timestamp").read[Int] | Reads.pure(0))~
      ((JsPath \ "rssi").read[Double] | Reads.pure(0.0))~
      ((JsPath \ "place_id").read[Int] | Reads.pure(0))
    )(nearestData.apply _)

  implicit def jsonWrites = Json.writes[nearestData]
}

case class exCloudBtxData(
  btx_id: Int,
  pos_id: Int,
  device_id: Int,
  updatetime: String,
  phase: Int,
  nearest: List[nearestData],
  power_level: Int
)

object exCloudBtxData {

  implicit val jsonReads: Reads[exCloudBtxData] = (
    ((JsPath \ "btx_id").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "pos_id").read[Int] | Reads.pure(0)) ~
      ((JsPath \ "device_id").read[Int] | Reads.pure(0))~
      ((JsPath \ "updatetime").read[String] | Reads.pure(""))~
      ((JsPath \ "phase").read[Int] | Reads.pure(0))~
      ((JsPath \ "nearest").read[List[nearestData]] | Reads.pure(List[nearestData]()))~
      ((JsPath \ "power_level").read[Int] | Reads.pure(0))
    )(exCloudBtxData.apply _)

  implicit def jsonWrites = Json.writes[exCloudBtxData]
}



