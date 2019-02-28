package services.analysis

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

import controllers.BeaconService
import javax.inject.Inject
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TelemetryService @Inject()(
  companyDAO: models.manage.companyDAO,
  ws: WSClient,
  exbDao:models.system.ExbDAO,
  beaconService: BeaconService,
  placeDAO: models.placeDAO
) {

  var GATEWAY_API_URL =""
  var TELEMETRY_API_URL =""
  /**
    * EXB状態取得
    *
    * EXB状態情報を取得する
    *
    * @param placeId  接続現場情報
    * @return  List[ExbTelemetryData]
    */
  def getTelemetryState(placeId:Int): Seq[ExbTelemetryData] = {
      val vPlaceDao = placeDAO.selectPlaceList(Seq[Int](placeId)).last
      TELEMETRY_API_URL = vPlaceDao.exbTelemetryUrl
      val telemetryList = Await.result(ws.url(TELEMETRY_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[telemeryState]].getOrElse(Nil)
      }, Duration.Inf)
      val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
      telemetryList.map { v =>
        val exbDatas =exbDao.selectExbApiInfo(placeId,v.description.toInt)
        var vExbPosName = "未検知"
        var vExbName = "未検知"
        var vTimeStamp = "未検知"
        var vStatus = "未検知"
        var vIBeaconTime = "未検知"

        if(exbDatas.length > 0){
          vExbPosName = exbDatas.last.exb_pos_name
          vExbName = exbDatas.last.exb_device_name
          val vCurrentEpochTime = System.currentTimeMillis()
          if (v.ibeacon_received < vCurrentEpochTime - 60*60*24*1000) { // 24時間以上前
            vStatus = "受信不良"
          } else {
            if (v.timestamp < (vCurrentEpochTime - 60 * 60 * 0.5 * 1000)) { // 30分間以上前
              vStatus = "動作不良"
            }else{
              vStatus = "正常"
            }
          }
          // epochを現在時間へ
          vTimeStamp = mSimpleDateFormat.format(new Timestamp(v.timestamp))
          vIBeaconTime = mSimpleDateFormat.format(new Timestamp(v.ibeacon_received))
        }
        ExbTelemetryData(
          v.description
          ,v.power_level
          ,vExbName
          ,vExbPosName
          , vTimeStamp
          ,vIBeaconTime
          , vStatus
        )
      }
  }


  /**
    * GW状態取得
    *
    * GW状態情報を取得する
    *
    * @param placeId  接続現場情報
    * @return  List[gateWayState]
    */
  def getGatewayState(placeId:Int): Seq[GwTelemetryData] = {
    val vPlaceDao = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    GATEWAY_API_URL = vPlaceDao.gatewayTelemetryUrl
    val gatewayList = Await.result(ws.url(GATEWAY_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[gateWayState]].getOrElse(Nil)
    }, Duration.Inf)
    val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
    gatewayList.map { v=>
      var vTimeStamp = "未検知"
      var vStatus = "未検知"
      var vUpdated = "未検知"

      if(v.updated != 0){
        val vCurrentEpochTime = System.currentTimeMillis()
        if (v.updated < vCurrentEpochTime - 60*60*24*1000) { // 24時間以上前
          vStatus = "受信不良"
        } else {
          if (v.updated < (vCurrentEpochTime - 60 * 60 * 0.5 * 1000)) { // 30分間以上前
            vStatus = "動作不良"
          }else{
            vStatus = "正常"
          }
        }
        // epochを現在時間へ
        vTimeStamp = mSimpleDateFormat.format(new Timestamp(v.timestamp))
        vUpdated = mSimpleDateFormat.format(new Timestamp(v.updated))
      }
      GwTelemetryData(
        v.num
        ,v.deviceid
        , vUpdated
        , vTimeStamp
        ,vStatus
      )
    }
  }
}
