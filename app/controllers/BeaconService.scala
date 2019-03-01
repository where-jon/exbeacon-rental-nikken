package controllers

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date, Locale}

import akka.util.Timeout
import javax.inject.Inject
import models.manage.{CarReserveViewer, CarViewer}
import models.{beaconPosition, _}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class CReserveData(
  var reserveDate :String
  ,var reserveAmCompany :String
  ,var reservePmCompany :String
  ,var reserveAmWorkType :String
  ,var reservePmWorkType :String
  ,var reserveRealDate :String
)

class BeaconService @Inject() (config: Configuration,
  ws: WSClient
  , val messagesApi: MessagesApi
  , carDAO: models.manage.ItemCarDAO
  ,exbDao:models.system.ExbDAO
  ,otherDAO: models.itemOtherDAO
  ,placeDAO: models.placeDAO
  ,txHelper: models.TxStatusHelper
  ) extends Controller with I18nSupport{
  private[this] implicit val timeout = Timeout(300, TimeUnit.SECONDS)
  var POS_API_URL =""
  var GATEWAY_API_URL =""
  var TELEMETRY_API_URL =""

  /** 予約の際現在時刻から予約に正しいかを判定する*/
  def currentTimeReserveCheck (vStartDate:String,vWorkType:String): String = {
     // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt

    if(mTime == vStartDate) { // 現在時刻と同じ日に予約
      if(mHour < 8 ) "OK"  // 現在時間が8時（作業まえの場合はなんでも予約OK）
      else if(mHour < 13 && vWorkType =="午後" ) "OK" // 現在時間が13時以前（午後はOK）
      else "当日"
    }else if (mTime > vStartDate){  // 現在時間より過去の方予約はNG
      "error.site.reserve.preTime"
    }else if (mTime < vStartDate) {  // 現在時間より未来の方+1day OK
      "OK"
    }else // 変な場合
      "error.site.reserve.other"
  }

  /** 予約取消の際現在時刻から予約取消に正しいかを判定する*/
  def currentTimeCancelCheck (vStartDate:String,vWorkType:String): String = {
    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt

    if(mTime == vStartDate) { // 現在時刻と同じ日に予約
      if(mHour < 8 ) "OK"  // 現在時間が8時（作業まえの場合はなんでも予約OK）
      else if(mHour < 13 && vWorkType =="午後" ) "OK" // 現在時間が13時以前（午後はOK）
      else Messages("error.site.cancel.overtime")
    }else if (mTime > vStartDate){  // 現在時間より過去の方予約はNG
      Messages("error.site.cancel.pretime")
    }else if (mTime < vStartDate) {  // 現在時間より未来の方+1day OK
      "OK"
    }else // 変な場合
      Messages("error.site.cancel.other")
  }

  /** 現在位置状態を判定*/
  def getCurrentPositionStatus (vPosId:Int,vUpdateTime:String): String = {
    val txStatusTemp = txHelper.getTxStatus(vPosId, vUpdateTime)
    val vExbName = if (txStatusTemp.getTxStatus() == 1) {
      "不在"
    } else if (txStatusTemp.getTxStatus() == 0) {
      "未検知"
    } else{
      "在席"
    }
    return vExbName
  }

  def getCloudUrl(placeId:Int): Boolean = {
    val vPlaceDao = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    POS_API_URL = vPlaceDao.btxApiUrl
    GATEWAY_API_URL = vPlaceDao.gatewayTelemetryUrl
    TELEMETRY_API_URL = vPlaceDao.exbTelemetryUrl
    try{
      var vCheck = true;
      var vPos = ws.url(POS_API_URL).get()
      var vGateway = ws.url(GATEWAY_API_URL).get()
      var vTelemetry = ws.url(TELEMETRY_API_URL).get()
      val posList = Await.result(vPos.map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
      val gatewayList = Await.result(vGateway.map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
      val telemetryList = Await.result(vTelemetry.map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
      vCheck=
        if(posList.size>0 ||gatewayList.size>0||telemetryList.size>0) true
        else false
      return vCheck
    } catch {
      case ignored: NullPointerException =>
        return false
    }
  }

  def setUpdateTime(jsonTime:String): String = {
    // iso時間変換
    val vUpdateTime = if (jsonTime == "") "" else {
      val dateTime = ZonedDateTime.parse(jsonTime, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()))
      dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
    }
    return vUpdateTime
  }


  /**
    * ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_car_masterテーブルと結合してitemCarBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めた作業車・立馬情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemCarBeaconPositionData]
    */
  def getItemCarReserveBeaconPosition(dbDatas:Seq[CarReserveViewer],arReserveDays:Seq[GetOneWeekData], blankInclude: Boolean = false, placeId:Int): Seq[itemCarReserveBeaconPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
        dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_car_btx_id) match {
          case Some(check) =>
            posList.find(_.btx_id == v.item_car_btx_id)
          case None => return null
        }

        var vUpdateTime = ""
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        val blankTargetMode =
          if (blankInclude && bpd.get.pos_id != -1) true else false
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName

        var reserveData = List[CReserveData]()
        // 予約関連
        arReserveDays.zipWithIndex.foreach { case (day, dayIndex) =>
          reserveData = reserveData :+ CReserveData("noDate","noAmCompany","noPmCompany","noAmWorkType","noPmWorkType",day.getDay)
        }

        if(v.ar_reserve_date(0)!=""){
          v.ar_reserve_date.zipWithIndex.foreach { case (reserve, reserveIndex) =>
            arReserveDays.zipWithIndex.foreach { case (day, dayIndex) =>
              if(day.getDay == reserve){
                val vCompany = v.ar_reserve_company_name(reserveIndex)
                val vWorkType = v.ar_reserve_work_type(reserveIndex)
                reserveData(dayIndex).reserveDate = reserve
                if(vWorkType == "午前"){
                  reserveData(dayIndex).reserveAmWorkType = vWorkType
                  reserveData(dayIndex).reserveAmCompany = vCompany
                }else if(vWorkType =="午後"){
                  reserveData(dayIndex).reservePmWorkType = vWorkType
                  reserveData(dayIndex).reservePmCompany = vCompany
                }else if(vWorkType =="終日"){
                  reserveData(dayIndex).reserveAmWorkType = vWorkType
                  reserveData(dayIndex).reservePmWorkType = vWorkType
                  reserveData(dayIndex).reserveAmCompany = vCompany
                  reserveData(dayIndex).reservePmCompany = vCompany
                }
              }
            }
          }
        }

        if (bpd.isDefined && blankTargetMode) {
          val vPosId =
            if (vExbName != "不在" ) bpd.get.pos_id else -1
          exbDatas.map { index =>
            vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
            vExbName = index.exb_pos_name
          }

          // jsonからiso時間変換
          vUpdateTime = this.setUpdateTime(bpd.get.updatetime)

          itemCarReserveBeaconPositionData(
            vExbName,
            vFloorName,
            bpd.get.btx_id,
            vPosId,
            bpd.get.phase,
            bpd.get.power_level,
            vUpdateTime,
            v.item_car_id,
            v.item_car_btx_id,
            v.item_car_key_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.item_car_no,
            v.item_car_name,
            v.place_id
            ,reserveData
          )
        } else {
          itemCarReserveBeaconPositionData(vExbName,vFloorName,-1, -1, -1, 0, "no",
            v.item_car_id,
            v.item_car_btx_id,
            v.item_car_key_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.item_car_no,
            v.item_car_name,
            v.place_id
            ,reserveData
          )
        }
      }.sortBy(_.item_car_btx_id)
  }


  /**
    * ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_car_masterテーブルと結合してitemCarBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めた作業車・立馬情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemCarBeaconPositionData]
    */
  def getItemCarBeaconPosition(dbDatas:Seq[CarViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemCarBeaconPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
        dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_car_btx_id) match {
          case Some(check) =>
            posList.find(_.btx_id == v.item_car_btx_id)
          case None => return null
        }
        var vUpdateTime = ""
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        val blankTargetMode =
          if (blankInclude && bpd.get.pos_id != -1) true else false
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName

        if (bpd.isDefined && blankTargetMode) {
          val vPosId =
            if (vExbName != "不在" ) bpd.get.pos_id else -1
          exbDatas.map { index =>
            vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
            vExbName = index.exb_pos_name
          }

          // jsonからiso時間変換
          vUpdateTime = this.setUpdateTime(bpd.get.updatetime)

          itemCarBeaconPositionData(
            vExbName,
            vFloorName,
            bpd.get.btx_id,
            vPosId,
            bpd.get.phase,
            bpd.get.power_level,
            vUpdateTime,
            v.item_car_id,
            v.item_car_btx_id,
            v.item_car_key_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.reserve_floor_name,
            v.item_car_no,
            v.item_car_name,
            v.place_id,
            v.reserve_start_date,
            v.company_id,
            v.company_name,
            v.work_type_id,
            v.work_type_name,
            v.reserve_id
          )
        } else {
          itemCarBeaconPositionData(vExbName,vFloorName,-1, -1, -1, 0, "no",
            v.item_car_id,
            v.item_car_btx_id,
            v.item_car_key_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.reserve_floor_name,
            v.item_car_no,
            v.item_car_name,
            v.place_id,
            v.reserve_start_date,
            v.company_id,
            v.company_name,
            v.work_type_id,
            v.work_type_name,
            v.reserve_id
          )
        }
      }.sortBy(_.item_car_btx_id)
  }


  /**
    * ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_other_masterテーブルと結合してitemOtherBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めたその他仮設材情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemOtherBeaconPositionData]
    */
  def getItemOtherBeaconPosition(dbDatas:Seq[OtherViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemOtherBeaconPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)

      dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_other_btx_id) match {
          case Some(check) =>
            posList.find(_.btx_id == v.item_other_btx_id)
          case None => return null
        }
        var vUpdateTime = ""
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        val blankTargetMode =
          if (blankInclude && bpd.get.pos_id != -1) true else false
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName
        if (bpd.isDefined && blankTargetMode) {
          val vPosId =
            if (vExbName != "不在" ) bpd.get.pos_id else -1
          exbDatas.map { index =>
            vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
            vExbName =  index.exb_pos_name
          }
          vUpdateTime = this.setUpdateTime(bpd.get.updatetime)
          itemOtherBeaconPositionData(
            vExbName,
            vFloorName,
            bpd.get.btx_id,
            vPosId,
            bpd.get.phase,
            bpd.get.power_level,
            vUpdateTime,
            v.item_other_id,
            v.item_other_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.reserve_floor_name,
            v.item_other_no,
            v.item_other_name,
            v.place_id,
            v.reserve_start_date,
            v.reserve_end_date,
            v.company_id,
            v.company_name,
            v.work_type_id,
            v.work_type_name,
            v.reserve_id
          )
        } else {
          itemOtherBeaconPositionData(vExbName,vFloorName,-1, -1, -1, 0, "no",
            v.item_other_id,
            v.item_other_btx_id,
            v.item_type_id,
            v.item_type_name,
            v.reserve_floor_name,
            v.item_other_no,
            v.item_other_name,
            v.place_id,
            v.reserve_start_date,
            v.reserve_end_date,
            v.company_id,
            v.company_name,
            v.work_type_id,
            v.work_type_name,
            v.reserve_id
          )
        }
      }.sortBy(_.item_other_btx_id)
  }


  /**
    * ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_other_masterテーブルと結合してitemOtherBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めたその他仮設材情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemOtherBeaconPositionData]
    */
  def getBeaconPosition(dbDatas:Seq[BeaconViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemBeaconPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)

      dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_btx_id) match {
            case Some(check) =>
              posList.find(_.btx_id == v.item_btx_id)
            case None => return null
        }
        var vUpdateTime = ""
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName
        val blankTargetMode =
          if (blankInclude && bpd.get.pos_id != -1) true else false
        if (bpd.isDefined && blankTargetMode) {
          val vPosId =
            if (vExbName != "不在" ) bpd.get.pos_id else -1
          exbDatas.map { index =>
            vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
            vExbName = index.exb_pos_name
          }
          vUpdateTime = this.setUpdateTime(bpd.get.updatetime)
          itemBeaconPositionData(
            vExbName,
            vFloorName,
            bpd.get.btx_id,
            vPosId,
            bpd.get.power_level,
            vUpdateTime,
            v.item_id,
            v.item_btx_id,
            v.item_key_btx,
            v.item_type_id,
            v.item_type_name,
            v.item_no,
            v.item_name,
            v.place_id,
            v.item_type_icon_color,
            v.item_type_text_color,
            v.company_name,
            v.work_type_name,
            v.reserve_floor_name,
            v.reserve_id,
            v.reserve_start_date,
            v.reserve_end_date
          )
        } else {
          itemBeaconPositionData(vExbName,vFloorName,-1, -1, 0, "no",
            v.item_id,
            v.item_btx_id,
            v.item_key_btx,
            v.item_type_id,
            v.item_type_name,
            v.item_no,
            v.item_name,
            v.place_id,
            v.item_type_icon_color,
            v.item_type_text_color,
            v.company_name,
            v.work_type_name,
            v.reserve_floor_name,
            v.reserve_id,
            v.reserve_start_date,
            v.reserve_end_date
          )
        }
      }.sortBy(_.item_btx_id)
  }


  /**
    * TX電池残量用データ取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_other_masterテーブルと結合してitemOtherBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めたその他仮設材情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemOtherBeaconPositionData]
    */
  def getTxData(dbDatas:Seq[BeaconViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemBeaconPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)
      dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_btx_id) match {
          case Some(check) =>
            posList.find(_.btx_id == v.item_btx_id)
          case None => return null
        }
        var vUpdateTime = ""
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName
        vUpdateTime = this.setUpdateTime(bpd.get.updatetime)
        val vPosId =
          if (vExbName != "不在" ) bpd.get.pos_id else -1
        exbDatas.map { index =>
          vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
          vExbName = index.exb_pos_name
        }
        itemBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          vPosId,
          bpd.get.power_level,
          vUpdateTime,
          v.item_id,
          v.item_btx_id,
          v.item_key_btx,
          v.item_type_id,
          v.item_type_name,
          v.item_no,
          v.item_name,
          v.place_id,
          v.item_type_icon_color,
          v.item_type_text_color,
          v.company_name,
          v.work_type_name,
          v.reserve_floor_name,
          v.reserve_id,
          v.reserve_start_date,
          v.reserve_end_date
        )
      }.sortBy(_.item_btx_id)
  }


  /**
    * itemLog用ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_other_masterテーブルと結合してitemOtherBeaconPositionDataのリストとして返却する
    *
    * @param dbDatas  予約テーブルまで含めたその他仮設材情報
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemOtherBeaconPositionData]
    */
  def getItemLogPosition(dbDatas:Seq[BeaconViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemLogPositionData] = {
      val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
        Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
      }, Duration.Inf)

      dbDatas.map { v =>
        val bpd = posList.find(_.btx_id == v.item_btx_id) match {
          case Some(check) =>
            posList.find(_.btx_id == v.item_btx_id)
          case None => return null
        }
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        var vExbName = this.getCurrentPositionStatus(bpd.get.pos_id,bpd.get.updatetime)
        var vFloorName = vExbName
        var vKeyFloorName = ""
        var vWorkFlg = false
        var vUpdateTime = ""
        val blankTargetMode =
          if (blankInclude && bpd.get.pos_id != -1) true else false
        if (bpd.isDefined && blankTargetMode) {
          val vPosId =
            if (vExbName != "不在" ) bpd.get.pos_id else -1
          exbDatas.map { index =>
            vFloorName = if (vExbName == "不在" ) "不在" else index.cur_floor_name
            vExbName = index.exb_pos_name
          }
          val bpdKey = posList.find(_.btx_id == v.item_key_btx)
          if(bpdKey.isDefined){
            val exbDatasKey = exbDao.selectExbApiInfo(placeId,bpdKey.get.pos_id)
            exbDatasKey.map { index =>
              vKeyFloorName = index.cur_floor_name
            }
            if(vFloorName == vKeyFloorName){
              vWorkFlg = true;
            }
          }
          // jsonからiso時間変換
          vUpdateTime = this.setUpdateTime(bpd.get.updatetime)

          itemLogPositionData(
            vExbName,
            vFloorName,
            bpd.get.btx_id,
            vPosId,
            vWorkFlg,
            vUpdateTime,
            v.item_id,
            v.item_btx_id,
            v.item_key_btx,
            v.item_type_id,
            v.item_type_name,
            v.item_no,
            v.item_name,
            v.place_id,
            v.item_type_icon_color,
            v.item_type_text_color,
            v.company_name,
            v.work_type_name,
            v.reserve_floor_name,
            v.reserve_id,
            v.reserve_start_date,
            v.reserve_end_date
          )
        } else {
          itemLogPositionData(vExbName,vFloorName,-1, -1, vWorkFlg, "no",
            v.item_id,
            v.item_btx_id,
            v.item_key_btx,
            v.item_type_id,
            v.item_type_name,
            v.item_no,
            v.item_name,
            v.place_id,
            v.item_type_icon_color,
            v.item_type_text_color,
            v.company_name,
            v.work_type_name,
            v.reserve_floor_name,
            v.reserve_id,
            v.reserve_start_date,
            v.reserve_end_date
          )
        }
      }.sortBy(_.item_btx_id)
  }



  /**
    * GW状態取得
    *
    * EXCloudIfActorにて非同期で取得しているGW状態情報を取得する
    *
    * @param placeId  接続現場情報
    * @return  List[gateWayState]
    */
  def getGateWayState(placeId:Int): Seq[gateWayState] = {

    this.getCloudUrl(placeId)
    val gatewayList = Await.result(ws.url(GATEWAY_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[gateWayState]].getOrElse(Nil)
    }, Duration.Inf)

    gatewayList
  }

}
