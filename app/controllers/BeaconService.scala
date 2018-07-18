package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.util.Timeout
import models._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BeaconService @Inject() (config: Configuration,
                               ws: WSClient
                               , carDAO: models.itemCarDAO
                               , @Named("SaveBtxDataActor") excIfActor: ActorRef
                               ,exbDao:models.ExbDAO
                               ,otherDAO: models.itemOtherDAO
                              ) extends Controller {
  private[this] implicit val timeout = Timeout(300, TimeUnit.SECONDS)


//  def getBeaconPositionViewList(placeId:Int) = Action {
//    val bpList = getBeaconPosition(true,placeId)
//    Ok(Json.toJson(bpList))
//  }

  /**
    * ビーコン位置取得
    *
    * EXCloudIfActorにて非同期で取得しているビーコン位置情報を取得する
    * その際、item_car_masterテーブルと結合してitemCarBeaconPositionDataのリストとして返却する
    *
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemCarBeaconPositionData]
    */
  def getItemCarBeaconPosition(blankInclude: Boolean = false, placeId:Int): Seq[itemCarBeaconPositionData] = {
    val dbDatas = carDAO.selectCarMasterViewer(placeId)
    //val f = excIfActor ? GetBtxPosition
   // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val url = config.getString("exbeacon.excloudUrl").get
    val posList = Await.result(ws.url(url).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_car_btx_id)
      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      val blankTargetMode = if (blankInclude) true else true
      if (bpd.isDefined && blankTargetMode) {
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        var vFloorName = "検知フロア無"
        var vExbName = "検知EXB無"
        var vTest = "検知EXB無"
          exbDatas.map { index =>
            vFloorName = index.cur_floor_name
            vExbName = index.exb_device_name
          }

        itemCarBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
          bpd.get.phase,
          bpd.get.power_level,
          bpd.get.updatetime,
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
        itemCarBeaconPositionData("現在フロア","現在位置",-1, -1, -1, 0, "nodate",
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
    * @param blankInclude  ブランクレコード（名前=param01が空）を含めるかどうか
    * @param placeId  接続現場情報
    * @return  List[itemOtherBeaconPositionData]
    */
  def getItemOtherBeaconPosition(blankInclude: Boolean = false, placeId:Int): Seq[itemOtherBeaconPositionData] = {
    val dbDatas = otherDAO.selectOtherMasterViewer(placeId)
    //val f = excIfActor ? GetBtxPosition
    // val posList = Await.result(f, timeout.duration).asInstanceOf[List[beaconPosition]]

    val url = config.getString("exbeacon.excloudUrl").get
    val posList = Await.result(ws.url(url).get().map { response =>
      Json.parse(response.body).asOpt[List[beaconPosition]].getOrElse(Nil)
    }, Duration.Inf)

    val bplist = dbDatas.map { v =>
      val bpd = posList.find(_.btx_id == v.item_other_btx_id)
      val floor = bpd.find(_.btx_id == bpd.get.pos_id)
      //!bpd.get.btx_name.isEmpty
      val blankTargetMode = if (blankInclude) true else true
      if (bpd.isDefined && blankTargetMode) {
        val exbDatas =exbDao.selectExbApiInfo(placeId,bpd.get.pos_id)
        var vFloorName = "検知フロア無"
        var vExbName = "検知EXB無"
        var vTest = "検知EXB無"
        exbDatas.map { index =>
          vFloorName = index.cur_floor_name
          vExbName = index.exb_device_name
        }

        itemOtherBeaconPositionData(
          vExbName,
          vFloorName,
          bpd.get.btx_id,
          bpd.get.pos_id,
          bpd.get.phase,
          bpd.get.power_level,
          bpd.get.updatetime,
          v.item_other_id,
          v.item_other_btx_id,
          v.item_type_id,
          v.item_type_name,
          v.reserve_floor_name,
          v.item_other_no,
          v.item_other_name,
          v.place_id,
          v.reserve_start_date,
          v.company_id,
          v.company_name,
          v.work_type_id,
          v.work_type_name,
          v.reserve_id
        )
      } else {
        itemOtherBeaconPositionData("現在フロア","現在位置",-1, -1, -1, 0, "nodate",
          v.item_other_id,
          v.item_other_btx_id,
          v.item_type_id,
          v.item_type_name,
          v.reserve_floor_name,
          v.item_other_no,
          v.item_other_name,
          v.place_id,
          v.reserve_start_date,
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
}
