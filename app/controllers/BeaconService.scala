package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.util.Timeout
import models._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BeaconService @Inject() (config: Configuration,
                               ws: WSClient
                               , val messagesApi: MessagesApi
                               , carDAO: models.itemCarDAO
                               ,exbDao:models.ExbDAO
                               ,otherDAO: models.itemOtherDAO
                               ,placeDAO: models.placeDAO
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
      Messages("error.site.reserve.pretime")
    }else if (mTime < vStartDate) {  // 現在時間より未来の方+1day OK
      "OK"
    }else // 変な場合
      Messages("error.site.reserve.other")
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

  def getCloudUrl(placeId:Int): Unit = {
    var vPlaceDao = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    POS_API_URL = vPlaceDao.btxApiUrl
    GATEWAY_API_URL = vPlaceDao.gatewayTelemetryUrl
    TELEMETRY_API_URL = vPlaceDao.exbTelemetryUrl
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
  def getItemCarBeaconPosition(dbDatas:Seq[CarViewer], blankInclude: Boolean = false, placeId:Int): Seq[itemCarBeaconPositionData] = {

    this.getCloudUrl(placeId)
    //val f = excIfActor ? GetBtxPosition
   // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_car_btx_id)
      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      val blankTargetMode = if (blankInclude) true else true
      var vExbName = "検知位置無"
      var vFloorName = "検知フロア無"

      if (bpd.isDefined && blankTargetMode) {
        val exbDatas = exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
          exbDatas.map { index =>
            vExbName = index.exb_pos_name
            vFloorName = index.cur_floor_name
          }

        // jsonからiso時間変換
        val vUpdateTime = this.setUpdateTime(bpd.get.updatetime)

        itemCarBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
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

    bplist
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

    this.getCloudUrl(placeId)
    //val f = excIfActor ? GetBtxPosition
    // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_other_btx_id)
      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      val blankTargetMode = if (blankInclude) true else true
      var vExbName = "検知位置無"
      var vFloorName = "検知フロア無"
      if (bpd.isDefined && blankTargetMode) {
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        exbDatas.map { index =>
          vExbName = index.exb_pos_name
          vFloorName = index.cur_floor_name
        }
        val vUpdateTime = this.setUpdateTime(bpd.get.updatetime)
        itemOtherBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
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

    bplist
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

    this.getCloudUrl(placeId)
    //val f = excIfActor ? GetBtxPosition
    // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_btx_id)
      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      var vExbName = "検知位置無"
      var vFloorName = "検知フロア無"
      val blankTargetMode = if (blankInclude) true else true
      if (bpd.isDefined && blankTargetMode) {
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        exbDatas.map { index =>
          vExbName = index.exb_pos_name
          vFloorName = index.cur_floor_name
        }
        val vUpdateTime = this.setUpdateTime(bpd.get.updatetime)
        itemBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
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

    bplist
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

    this.getCloudUrl(placeId)
    //val f = excIfActor ? GetBtxPosition
    // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val posList = Await.result(ws.url(POS_API_URL).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_btx_id)

      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      var vExbName = "検知位置無"
      var vFloorName = "検知フロア無"
      var vKeyFloorName = ""
      var vWorkFlg = false
      val blankTargetMode = if (blankInclude) true else true
      if (bpd.isDefined && blankTargetMode) {
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        exbDatas.map { index =>
          vExbName = index.exb_pos_name
          vFloorName = index.cur_floor_name
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
        val vUpdateTime = this.setUpdateTime(bpd.get.updatetime)

        itemLogPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
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

    bplist
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
