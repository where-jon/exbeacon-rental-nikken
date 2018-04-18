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
                               ) extends BaseController with I18nSupport {

  /**
    * 初期表示
    * @return
    */

  def index = SecuredAction.async { implicit request =>
    val placeId = super.getCurrentPlaceId
    // 現場情報
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last
    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 作業車情報
    val carList = carDAO.selectCarInfo(placeId)
    // 業者情報
    val companyList = companyDAO.selectCompany(placeId)

    // そのフロアの予約を取得
    val reserveInfo = carSummeryDAO.selectReserve(dateStr = new DateTime().toString("yyyyMMdd"), placeId = Option(placeId))

    // 全数情報
    var reserveCntTotal = 0
    var normalWorkingCntTotal = 0
    var workingOnlyCntTotal = 0
    var reserveOnlyCntTotal = 0
    var noReserveNoWorkingCntTotal = 0

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>

      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // フロア毎に処理
      var resultList = floorInfoList.map{floor => // -- ループ start --
        var normalWorkingCnt = 0
        var workingOnlyCnt = 0
        var reserveOnlyCnt = 0
        var noReserveNoWorkingCnt = 0

        // 実際の作業車Tx
        val carsAtFloor = list
          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
          .filter(carList.map{c => c.carBtxId} contains _.btx_id)  // 予約作業車のBTXidに合致するもの

        // 実際の鍵Tx
        val keysAtFloor = list
          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
          .filter(carList.map{c => c.carKeyBtxId} contains _.btx_id)  // 予約作業車鍵のBTXidに合致するもの

        // 実際の作業車毎に処理
        carsAtFloor.foreach(car => { // -- foreach start --
          val carRest = carList.filter(_.carBtxId == car.btx_id)
          if(carRest.length > 0){
            val rest = reserveInfo.filter(_.carBtxId == car.btx_id).filter(_.floorId == floor.floorId)
            if (rest.isEmpty == false) {
              // 予約あり
              val result = keysAtFloor.filter(_.btx_id == rest.last.carKeyBtxId)
              if (result.length > 0) {
                // 稼働中
                normalWorkingCnt += 1
              } else {
                // 非稼働
                reserveOnlyCnt += 1
              }
            } else {
              // 予約なし
              val keyRest = keysAtFloor.filter(_.btx_id == carRest.last.carKeyBtxId)
              if(keyRest.length > 0){
                // 稼働中
                workingOnlyCnt += 1
              }else{
                noReserveNoWorkingCnt += 1
              }
            }
          }
        })// -- foreach end --

        // 各々の全数の値に加算
        reserveCntTotal += reserveInfo.filter(_.floorId == floor.floorId).length
        normalWorkingCntTotal += normalWorkingCnt
        workingOnlyCntTotal += workingOnlyCnt
        reserveOnlyCntTotal += reserveOnlyCnt
        noReserveNoWorkingCntTotal += noReserveNoWorkingCnt

        // レコード生成
        CarSummeryInfo(
            floor.floorName
          , reserveInfo.filter(_.floorId == floor.floorId).length
          , normalWorkingCnt
          , workingOnlyCnt
          , reserveOnlyCnt
          , noReserveNoWorkingCnt
        )
      } // -- ループ end --

      // 全数のレコードを追加
      resultList :+= CarSummeryInfo(
          Messages("lang.CarSummery.summeryTotal")
        , reserveCntTotal
        , normalWorkingCntTotal
        , workingOnlyCntTotal
        , reserveOnlyCntTotal
        , noReserveNoWorkingCntTotal
      )

      // 合計値の算出
      val allTotal = (normalWorkingCntTotal + workingOnlyCntTotal + reserveOnlyCntTotal + noReserveNoWorkingCntTotal)

      Ok(views.html.carSummery(companyList, floorInfoList, resultList, allTotal))
    }
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

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
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
          // ID, 作業車番号
          carIdStr = car.last.carId.toString//
          carNoStr = car.last.carNo//
          // フロア
          val floor = floorInfoList.filter(_.exbDeviceIdList contains apiData.device_id)
          if(floor.nonEmpty){
            floorIdStr = floor.last.floorId.toString//
          }else{
            //TODO DBから取得
          }
          // 業者
          val restReserveInfo = reserveInfo.filter(_.carId == car.last.carId)
          if(restReserveInfo.isEmpty == false){
            // 予約あり
            companyIdStr = restReserveInfo(0).companyId.toString//
          }else{
            // 前日予約無
          }
          // 稼働・非稼働
          val keyBtx = list.filter(_.btx_id == car.last.carKeyBtxId)
          if(keyBtx.nonEmpty){
            if(keyBtx.last.device_id != 0){
              val floor = floorInfoList.filter(_.exbDeviceIdList contains keyBtx.last.device_id)
              if(floor.nonEmpty){
                isWorking = (floor.last.floorId.toString == floorIdStr)
              }else{
                //フロア外デバイス、つまり鍵保管庫
                isWorking = false
              }
            }else{
              // TODO 未検知のため、DB履歴から取得

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

        // TODO DBに履歴を登録する


      }

      var a, b ,c, d, e = 0
      var aa, bb ,cc, dd, ee = 0

      // 集計
      var carSummeryInfo = floorInfoList.map { f =>

        a = carSummeryReservePlotInfoList.filter(_.floorId == f.floorId).length
        b = carSummeryWorkPlotInfoList.filter(_.floorId == f.floorId.toString).filter(_.isWorking == true).length
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

// -- 保持しないversion
//      // フロア毎に見てく
//      floorInfoList.foreach { floor =>
//
//        // 作業車
//        val carsAtFloor = list
//          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
//          .filter(carList.map{c => c.carBtxId} contains _.btx_id)  // 予約作業車のBTXidに合致するもの
//        // 鍵
//        val keysAtFloor = list
//          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
//          .filter(carList.map{c => c.carKeyBtxId} contains _.btx_id)  // 予約作業車鍵のBTXidに合致するもの
//
//        // 実体の車
//        carsAtFloor.foreach(car =>{
//
//          var floorIdStr = ""
//          var carIdStr = ""
//          var carNoStr = ""
//          var companyIdStr = ""
//          var isWorking = false
//
//          val carInfoList = carList.filter(_.carBtxId == car.btx_id)
//          if(carInfoList.isEmpty == false){
//            floorIdStr = floor.floorId.toString//
//            carIdStr = carInfoList.last.carId.toString//
//            carNoStr = carInfoList.last.carNo//
//
//            val restReserveInfo = reserveInfo.filter(_.carId == carInfoList.last.carId)
//            if(restReserveInfo.isEmpty == false){
//              // 予約あり
//              companyIdStr = restReserveInfo(0).companyId.toString//
//            }else{
//              // 前日予約無
//            }
//
//            val existList = keysAtFloor.filter(_.btx_id == carInfoList.last.carKeyBtxId)
//            if(existList.isEmpty == false){
//              // 稼働中
//              isWorking = true
//            }else{
//              // 非稼働
//              isWorking = false
//            }
//            // 値のセット
//            carSummeryWorkPlotInfoList :+= CarSummeryWorkPlotInfo(floorIdStr, carIdStr, carNoStr, companyIdStr, isWorking)
//          }else{
//            // DB登録なし
//          }
//        })
//      }
//
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
