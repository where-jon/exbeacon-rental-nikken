package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models._
import play.api._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws._
import utils.silhouette.MyEnv
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.joda.time.DateTime

/**
  * 高所作業車各階稼働状況アクションクラス
  *
  *
  */
@Singleton
class CarSummery @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , ws: WSClient
                           , carSummeryDAO: models.carSummeryDAO
                           , floorDAO: models.floorDAO
                           , placeDAO: models.placeDAO
                           , carDAO: models.carDAO
                           , companyDAO: models.companyDAO
                           , btxLastPositionDAO: models.btxLastPositionDAO
                               ) extends BaseController with I18nSupport {

  /**
    * 初期表示
    * @return
    */
  def index = SecuredAction { implicit request =>
    val placeId = super.getCurrentPlaceId
    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 業者情報
    val companyList = companyDAO.selectCompany(placeId)

    // 非稼働時間かどうか
    val isNoWorkTime = (new DateTime().toString("HHmm") < config.getString("noWorkTimeEnd").get)

    Ok(views.html.carSummery(companyList, floorInfoList, isNoWorkTime))
  }

  /**
    * 作業車稼働情報のJSON出力
    * @return
    */
  def getPlotInfo = SecuredAction.async { implicit request =>
    val placeId = super.getCurrentPlaceId

    // 予約情報
    val carSummeryReservePlotInfoList = carSummeryDAO.selectReserveForPlot(placeId, new DateTime().toString("yyyyMMdd"))
    // 稼働情報
    var carSummeryWorkPlotInfoList = Seq[CarSummeryWorkPlotInfo]()
    // 現場の情報を取得
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 作業車情報
    val carList = carDAO.selectCarInfo(placeId)
    // 予約情報
    val reserveInfo = carSummeryDAO.selectReserve(dateStr = new DateTime().toString("yyyyMMdd"), placeId = Option(placeId))
    // 履歴input
    var inputPosition = Seq[BtxLastPosition]()

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    // API呼び出し実行
    ws.url(url).get().map { response =>

      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      list.foreach{apiData =>

        var floorIdStr = ""
        var carIdStr = ""
        var carNoStr = ""
        var companyIdStr = ""
        var isWorking = false

        val car = carList.filter(_.carBtxId == apiData.btx_id)
        if(car.nonEmpty){
          // ID, 作業車番号 --
          carIdStr = car.last.carId.toString//
          carNoStr = car.last.carNo//
          // フロア --
          val floor = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
          if(floor.nonEmpty){
            floorIdStr = floor.last.floorId.toString//
          }else{
            // 履歴DBから取得
            val hist = btxLastPositionDAO.find(placeId, Seq[Int](car.last.carBtxId))
            if(hist.nonEmpty){
              floorIdStr = hist.last.floorId.toString
            }else{
              // 表示なし
              Logger.warn(s"履歴が無いため表示なし。現場ID = ${placeId}, 作業車番号 = ${carNoStr}, btx_id = ${apiData.btx_id}")
            }
          }
          // 業者 --
          val restReserveInfo = reserveInfo.filter(_.carId == car.last.carId)
          if(restReserveInfo.nonEmpty){
            // 予約あり
            companyIdStr = restReserveInfo(0).companyId.toString//
          }else{
            // 前日予約無
          }
          // 稼働・非稼働 --
          val keyBtx = list.filter(_.btx_id == car.last.carKeyBtxId)
          if(keyBtx.nonEmpty){
            if(keyBtx.last.device_id != 0){
              //val floor = floorInfoList.filter(_.exbDeviceIdList contains keyBtx.last.device_id.toString)
              val floor = utils.BtxUtil.getNearestFloor(floorInfoList, keyBtx.last)
              if(floor.nonEmpty){
                if(floor.last.floorId.toString == floorIdStr){
                  val isNoWorkTime = (new DateTime().toString("HHmm") < config.getString("noWorkTimeEnd").get)
                  if(isNoWorkTime){
                    isWorking = false
                  }else{
                    isWorking = true
                  }
                }else{
                  isWorking = false
                }
              }else{
                //フロア外デバイス、つまり鍵保管庫
                isWorking = false
              }
            }else{
              // 未検知のため、DB履歴から取得
              val hist = btxLastPositionDAO.find(placeId, Seq[Int](keyBtx.last.btx_id))
              if(hist.nonEmpty){
                if(hist.last.floorId.toString == floorIdStr){
                  val isNoWorkTime = (new DateTime().toString("HHmm") < config.getString("noWorkTimeEnd").get)
                  if(isNoWorkTime){
                    isWorking = false
                  }else{
                    isWorking = true
                  }
                }else{
                  isWorking = false
                }
              }else{
                isWorking = false
              }
            }
          }else{
            // 登録してる鍵TXがAPIにない
            Logger.debug(s"""登録してる鍵TXがAPIにない。TX番号：${car.last.carKeyBtxId}""")
            isWorking = false
          }

          // 値のセット
          carSummeryWorkPlotInfoList :+= CarSummeryWorkPlotInfo(floorIdStr, carIdStr, carNoStr, companyIdStr, isWorking)

        }else{
          // 作業車としてのDB未登録
          Logger.debug(s"""作業車としてのDB未登録。btx_id：${apiData.btx_id}""")
        }

        // 履歴のインプットを貯める
        val floors = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
        if(floors.nonEmpty){
          inputPosition :+= BtxLastPosition(apiData.btx_id, placeId, floors.last.floorId)
        }
      }//-- loop end

      // 履歴の登録
      btxLastPositionDAO.update(inputPosition)

      // 集計
      var a, b ,c, d, e = 0
      var aa, bb ,cc, dd, ee = 0
      var carSummeryInfo = floorInfoList.map { f =>
        a = carSummeryReservePlotInfoList.filter(_.floorId == f.floorId.toString).length
        b = carSummeryWorkPlotInfoList.filter(_.floorId == f.floorId.toString).filter(_.isWorking == true).filter(_.companyId.nonEmpty).length
        c = carSummeryWorkPlotInfoList.filter(_.floorId == f.floorId.toString).filter(_.isWorking == true).filter(_.companyId.isEmpty).length
        d = carSummeryWorkPlotInfoList.filter(_.floorId == f.floorId.toString).filter(_.isWorking == false).filter(_.companyId.nonEmpty).length
        e = carSummeryWorkPlotInfoList.filter(_.floorId == f.floorId.toString).filter(_.isWorking == false).filter(_.companyId.isEmpty).length
        aa += a
        bb += b
        cc += c
        dd += d
        ee += e
        CarSummeryInfo(f.floorName,a,b,c,d,e)
      }
      carSummeryInfo :+= CarSummeryInfo(Messages("lang.CarSummery.summeryTotal"), aa, bb, cc, dd, ee)

      // 最終結果
      val resultList = CarSummeryPlotInfo(
        carSummeryWorkPlotInfoList.toList
      , carSummeryReservePlotInfoList.toList
      , carSummeryInfo.toList
      , carSummeryWorkPlotInfoList.length
      )

      Ok(Json.toJson(resultList))
    }
  }

}
