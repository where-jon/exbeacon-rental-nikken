package controllers.analysis

import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService}
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.MyEnv



/*作業車稼働状況分析用クラス*/
case class WeekData(
  iNum :Int,
  iWeekStartDay :String,
  iWeekEndDay :String,
  iWeekTotalWorkDay :Int,
  iWeekRealWorkDay :Int,
  iWeekTotalTime :Int
)

/**
  * 作業車稼働状況分析画面
  *
  *
  */

@Singleton
class MovementCar @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
, carDAO: models.itemCarDAO
, companyDAO: models.companyDAO
, beaconService: BeaconService
, floorDAO: models.floorDAO
, btxDAO: models.btxDAO
, itemlogDAO: ItemLogDAO
, itemTypeDAO: models.ItemTypeDAO
, workTypeDAO: models.WorkTypeDAO
, calendarDAO: models.LogCalendarDAO
) extends BaseController with I18nSupport {

   /*検索用*/
  var DETECT_MONTH = "";
  val TOTAL_WORK_DAY = 7;
  val REAL_WORK_DAY = 5;
  val DAY_WORK_TIME = 6;
  val HOUR_MINUTE = 6;
  val BATCH_INTERVAL_MINUTE = 30;


  var DETECT_MONTH_DAY = "";
  var NOW_DATE = "";
  var TOTAL_LENGTH = 0;

  val movementCarSearchForm = Form(mapping(
    "inputDate" -> text
  )(MovementCarSearchData.apply)(MovementCarSearchData.unapply))

  /*登録用*/
  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id


  /** 　初期化 */
  def init(): Unit = {

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy/MM", Locale.JAPAN)
    val mSimpleDateFormat2 = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    DETECT_MONTH = mTime
    DETECT_MONTH_DAY += "-01"
    NOW_DATE = mSimpleDateFormat2.format(currentTime)
  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemOnlyCarInfo(_placeId);
    /*仮設材種別id取得*/
    itemIdList = itemTypeList.map{item => item.item_type_id}
    if(itemIdList.isEmpty){
      itemIdList = Seq(-1)
    }
  }


//  /** 　item_logテーブルデータ取得 */
//  def getAllItemLogData(placeId:Int,itemIdList :Seq[Int]) : Seq[List[Seq[MovementWorkingLog]]] = {
//    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)  // 作業車のみ
//    val carList = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
//    val calendarList = getCalendarData();
//    var workData = List[WorkRate]()
//    //var movementWorkingLog :Seq[MovementWorkingLog]
//
//    // ②①から取得したデータへgetCalndarDataを含む
//    val allData = carList.zipWithIndex.map { case (car, i) =>
//      calendarList.zipWithIndex.map{ case (calendar, i) =>
//       // if(car.item_car_id == calendar.)
//          val stam =itemlogDAO.selectWorkingOn(car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
//          val vOperatingRate = -1
//            //(stam.last.detected_count * BATCH_INTERVAL_MINUTE) / (calendar.iWeekTotalTime * 60) * 100
//          //return stam
//        //stam.last.item_car_id
//
////        workData :+
////          WorkRate(car.item_car_id,vOperatingRate,-1)
//
////        movementWorkingLog = movementWorkingLog :+
////          MovementWorkingLog(
////            stam.last.item_car_id
////            ,stam.last.item_car_name
////            ,stam.last.item_car_btx_id
////            ,stam.last.detected_count
////            ,stam.last.operating_rate
////            ,stam.last.reserve_operating_rate
////          )
////
//        itemlogDAO.selectWorkingOn(car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
//      }
//    }
//
//    return allData
//  }

  /** 　item_logテーブルデータ取得 */
  def getAllItemLogData(placeId:Int,itemIdList :Seq[Int]) : Seq[List[Seq[WorkRate]]] = {
    // ①itemCarテーブルlistからsqlを組むplaceId
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)  // 作業車のみ
    val carList = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    val calendarList = getCalendarData();
    var workData = List[WorkRate]()

    // ②①から取得したデータへgetCalndarDataを含む
    val allData = carList.zipWithIndex.map { case (car, i) =>
      car.item_car_id
      calendarList.zipWithIndex.map{ case (calendar, i) =>
        // if(car.item_car_id == calendar.)
        val getWorkFlgSqlData =itemlogDAO.selectWorkingOn(car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
        val vWofkFlgDetectCount = getWorkFlgSqlData.last.detected_count
        val vOperatingRate = if(vWofkFlgDetectCount == 0 || vWofkFlgDetectCount == -1){
          0
        }else{
          (getWorkFlgSqlData.last.detected_count * BATCH_INTERVAL_MINUTE) / (calendar.iWeekTotalTime * 60) * 100
        }

        workData :+
        WorkRate(car.item_car_id,car.item_car_name,vOperatingRate,-1)

      }
    }
    return allData
  }
  /** 　item_logテーブルデータ取得 */
  def getItemLogData(placeId:Int) : List[Seq[MovementWorkingLog]] = {
    // ①itemCarテーブルlistからsqlを組むplaceId
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)  // 作業車のみ
    val carList = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    val calendarList = getCalendarData();
    // ②①から取得したデータへgetCalndarDataを含む
//    var gg = carList.zipWithIndex.map { case (car, i) =>
//      calendarList.zipWithIndex.map{ case (calendar, i) =>
//        itemlogDAO.selectWorkingOn(placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
//      }
//    }

    val vItemData = calendarList.zipWithIndex.map { case (caledar, i) =>
//      iNum :Int,
//      iWeekStartDay :String,
//      iWeekEndDay :String,
//      iWeekTotalWorkDay :Int,
//      iWeekRealWorkDay :Int,
//      iWeekTotalTime :Int

      // 週を回しながらlistのカラムを更新
      itemlogDAO.selectWorkingOn(1,placeId,caledar.iWeekStartDay,caledar.iWeekEndDay,itemIdList)
    }
    return vItemData

  }
  /** 　検索側データ取得 */
  def getCalendarData(): List[WeekData] = {
    var bWeekLoofCheck = true
    var iWeekIndex = 1
    var weekData = List[WeekData]()
    TOTAL_LENGTH = 0
     while (bWeekLoofCheck) {
       TOTAL_LENGTH = TOTAL_LENGTH+ 1
       val vWeekFirstDay = calendarDAO.selectGetWeek(iWeekIndex,DETECT_MONTH).last.getDay
       var vWeekLastDay = calendarDAO.selectGetWeekMinusDay(iWeekIndex,-1,DETECT_MONTH).last.getDay
       val vTemp= vWeekFirstDay.splitAt(8)
       val vTempMonth= vWeekFirstDay.splitAt(7)
       val vCurrMonth = vTempMonth._1
       val intWeekFirstDay =vTemp._2.toInt
       var vRealWorkDay = 0
      if(iWeekIndex == 1){ // 最初の方だけ
        if(intWeekFirstDay == 8){ // 一周目が一日の場合普通に営業日5日
          val vWeekTotalTime = DAY_WORK_TIME * REAL_WORK_DAY
          weekData = weekData :+
            WeekData(1,DETECT_MONTH + "-01",vWeekLastDay,TOTAL_WORK_DAY,REAL_WORK_DAY,vWeekTotalTime)
        }else{  //一周目が一日じゃない場合営業日を出す必要がある
          val vTermDay =
            calendarDAO.selectGetTermDay(iWeekIndex,DETECT_MONTH,DETECT_MONTH+"-01").last.getDay.toInt
          if(vTermDay < 3){ // 営業日0
            vRealWorkDay = 0
          }else{  // 営業日は週末二日を除くので
            vRealWorkDay = vTermDay-2
          }
          val vWeekTotalTime = DAY_WORK_TIME * vRealWorkDay
          weekData = weekData :+
            WeekData(1,DETECT_MONTH + "-01",vWeekLastDay,vTermDay,vRealWorkDay,vWeekTotalTime)
        }
      }else{
        // indexが2番目から
        val vWeekBeforeFirstDay = calendarDAO.selectGetWeek(iWeekIndex-1,DETECT_MONTH).last.getDay
        var vTermDay = 0
        var vYasumiCount = 0
        if(vCurrMonth != DETECT_MONTH) { // 現在indexの元になる月が当月を過ぎた場合
          val vTargetDate = calendarDAO.selectGetLastMonthDay(DETECT_MONTH).last.getDay
          vTermDay =
            calendarDAO.selectGetTermStarEndDay(vTargetDate,vWeekBeforeFirstDay).last.getDay.toInt
          vWeekLastDay = vTargetDate
          bWeekLoofCheck = false  // もう次からは当月ではないのでloofを止めさえる
        }else{
          vTermDay =
            calendarDAO.selectGetTermDay(iWeekIndex,DETECT_MONTH,vWeekBeforeFirstDay).last.getDay.toInt
        }
        var vTermDayList = calendarDAO.selectGetGenerateDay(vWeekBeforeFirstDay,vWeekLastDay)
        vTermDayList.map{ v =>  // 範囲内の土(6.0)日(.0)を検索する
          val vYobi = calendarDAO.selectGetYoubi(v.getDay).last.getDay.trim()
          if( vYobi == "6.0" || vYobi == ".0"){
            vYasumiCount = vYasumiCount+1
          }
        }
        vRealWorkDay = vTermDay - vYasumiCount
        val vWeekTotalTime = DAY_WORK_TIME * vRealWorkDay
        weekData = weekData :+
          WeekData(iWeekIndex,vWeekBeforeFirstDay,vWeekLastDay,vTermDay,vRealWorkDay,vWeekTotalTime)
      }

      iWeekIndex = iWeekIndex + 1; // 週目カウントを増加

     }
    System.out.println("=========[" +DETECT_MONTH+"]=======")
    weekData.map{ v=>
      System.out.println("================")
      System.out.println("週目：" +v.iNum)
      System.out.println("週最初日：" +v.iWeekStartDay)
      System.out.println("週最終日：" +v.iWeekEndDay)
      System.out.println("週全日：" +v.iWeekTotalWorkDay)
      System.out.println("週働く日：" +v.iWeekRealWorkDay)
      System.out.println("週働く時間：" +v.iWeekTotalTime)
      System.out.println("================")
    }
    return weekData;
  }

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 検索情報
    val searchForm = movementCarSearchForm.bindFromRequest.get
    DETECT_MONTH = searchForm.inputDate

    // DB探索になる今月に関するデータをセット
    //val vCalederList = getCalendarData();

    //val dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
    //var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    val logItemData =  this.getItemLogData(placeId)
    val logItemAllData =  getAllItemLogData(placeId,itemIdList)
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)
    val carList = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    Ok(views.html.analysis.movementCar(logItemAllData,DETECT_MONTH,logItemData,carList,TOTAL_LENGTH))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 初期化
    init();
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // DB探索になる今月に関するデータをセット
    var logItemData =  this.getItemLogData(placeId)
    logItemData.map { v =>
      v.zipWithIndex.map { case (logData, i) =>
          System.out.println("================")
          System.out.println("carId：" +logData.item_car_id)
          System.out.println("carId：" +logData.item_car_name)
          System.out.println("carId：" +logData.item_car_btx_id)
          System.out.println("carId：" +logData.detected_count)
      }

    }

    val logItemAllData =  getAllItemLogData(placeId,itemIdList)

    // 全体から空いてるものだけ表示する。
    //carListApi = carListApi.filter(_.reserve_id == -1)
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)
    val carList = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    Ok(views.html.analysis.movementCar(logItemAllData,DETECT_MONTH,logItemData,carList,TOTAL_LENGTH))
  }

}
