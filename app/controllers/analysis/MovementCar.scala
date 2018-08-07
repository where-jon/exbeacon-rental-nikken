package controllers.analysis

import java.io.{File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService}
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


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
  val HOUR_MINUTE = 60;
  val BATCH_INTERVAL_MINUTE = 1;

  val CALENDAR_TYPE = "今月のみ";
  //val CALENDAR_TYPE = "今月以外";

  var DETECT_MONTH_DAY = "";
  var NOW_DATE = "";
  var TOTAL_LENGTH = 0;

  val movementCarSearchForm = Form(mapping(
    "inputDate" -> text
  )(MovementCarSearchData.apply)(MovementCarSearchData.unapply))

  /*登録用*/
  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id

  /*csv用*/
  private val CSV_HEAD = "#MONTH_MOVEMENT_CAR_V1"

  /** 　初期化 */
  def init(): Unit = {

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.JAPAN)
    val mSimpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
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

  /** 　作業時間を計算 */
  def getDetectedCount(detectedCount:Integer, weekTotalTime:Integer) : Float = {
    if(detectedCount == 0 || detectedCount == -1){
      0
    }else{
      (detectedCount * BATCH_INTERVAL_MINUTE) / (weekTotalTime * HOUR_MINUTE).toFloat * 100
    }
  }

  /** 　作業週の曜日を求める */
  def getYobi(targetDay:String) : String = {
    val vYobi = calendarDAO.selectGetYoubi(targetDay).last.getDay.trim()
    val szYobi =
    if( vYobi == "6.0" ) "土"
    else if (vYobi == ".0") "日"
    else if (vYobi == "1.0") "月"
    else if (vYobi == "2.0") "火"
    else if (vYobi == "3.0") "水"
    else if (vYobi == "4.0") "木"
    else if (vYobi == "5.0") "金"
    else  ""

    return szYobi
  }

  /** 　item_logテーブルデータ取得 */
  def getAllItemLogData(placeId:Int,itemIdList :Seq[Int],calendarList:List[WeekData]) : Seq[List[WorkRate]] = {
    // ①itemCarテーブルlistからsqlを組むplaceId
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)  // 作業車のみ

    // ②. ①から取得したデータへgetCalndarDataを含む
    val allData = dbDatas.zipWithIndex.map { case (car, i) =>
      calendarList.zipWithIndex.map{ case (calendar, i) =>

        // 検知フラグがtrue
        val getWorkFlgSqlData =itemlogDAO.selectWorkingOn(false,car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
        val vWofkFlgDetectCount = getWorkFlgSqlData.last.detected_count
        if(vWofkFlgDetectCount>0){
          System.out.println(vWofkFlgDetectCount)
        }
        val vOperatingRate = this.getDetectedCount(vWofkFlgDetectCount,calendar.iWeekTotalTime)
        // 予約ともに検知フラグがtrue
        val getReserveWorkFlgSqlData =itemlogDAO.selectReserveAndWorkingOn(false,false,car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
        val vReserveWofkFlgDetectCount = getReserveWorkFlgSqlData.last.detected_count
        val vReserveOperatingRate = this.getDetectedCount(vReserveWofkFlgDetectCount,calendar.iWeekTotalTime)

        WorkRate(car.item_car_id,car.item_car_name,vOperatingRate,vReserveOperatingRate)

      }
    }
    return allData
  }


  def getMonthData(): List[WeekData] = {

    var bWeekLoofCheck = true
    var iWeekIndex = 1
    var weekData = List[WeekData]()
    TOTAL_LENGTH = 0
    while (bWeekLoofCheck) {
      TOTAL_LENGTH = TOTAL_LENGTH+ 1
      val vWeekFirstDay = calendarDAO.selectGetWeek(iWeekIndex,DETECT_MONTH).last.getDay
      var vWeekLastDay = calendarDAO.selectGetWeekMinusDay(iWeekIndex+1,-1,DETECT_MONTH).last.getDay
      var vTemp= vWeekFirstDay.splitAt(8)
      val vTempMonth= vWeekFirstDay.splitAt(7)
      val vCurrMonth = vTempMonth._1
      val intWeekFirstDay = vTemp._2.toInt
      var vRealWorkDay = 0


        // indexが2番目から
        //val vNextWeekFirstDay = calendarDAO.selectGetWeek(iWeekIndex+1,DETECT_MONTH).last.getDay
        var vTermDay = 0
        var vYasumiCount = 0
        if(vCurrMonth != DETECT_MONTH) { // 現在indexの元になる月が当月を過ぎた場合
          val vTargetDate = calendarDAO.selectGetLastMonthDay(DETECT_MONTH).last.getDay
          vTermDay =
            calendarDAO.selectGetTermStarEndDay(vTargetDate,vWeekFirstDay).last.getDay.toInt
          vWeekLastDay = vTargetDate
          bWeekLoofCheck = false  // もう次からは当月ではないのでloofを止めさえる
          val szYobi = vTargetDate + "日("+ this.getYobi(vTargetDate) + ")"
        }else{
          vTermDay = calendarDAO.selectGetTermStarEndDay(vWeekLastDay,vWeekFirstDay).last.getDay.toInt
            //calendarDAO.selectGetTermDay(iWeekIndex,vNextWeekFirstDay,DETECT_MONTH).last.getDay.toInt

        }
        var vTermDayList = calendarDAO.selectGetGenerateDay(vWeekFirstDay,vWeekLastDay)
        vTermDayList.map{ v =>  // 範囲内の土(6.0)日(.0)を検索する
          val vYobi = calendarDAO.selectGetYoubi(v.getDay).last.getDay.trim()
          if( vYobi == "6.0" || vYobi == ".0"){
            vYasumiCount = vYasumiCount+1
          }
        }
        vRealWorkDay = vTermDay - vYasumiCount
        val vWeekTotalTime = DAY_WORK_TIME * vRealWorkDay
        vTemp= vWeekFirstDay.splitAt(8)
        val szYobi = intWeekFirstDay + "日("+ this.getYobi(vWeekFirstDay) + ")"
        weekData = weekData :+
          WeekData(szYobi,iWeekIndex,vWeekFirstDay,vWeekLastDay,vTermDay,vRealWorkDay,vWeekTotalTime)
      iWeekIndex = iWeekIndex + 1; // 週目カウントを増加
    }
    return weekData
  }

  def getOnlyThisMonthData(): List[WeekData] = {

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
        val szYobi =
          "1日("+ this.getYobi(DETECT_MONTH + "-01") + ")"

        if(intWeekFirstDay == 8){ // 一周目が一日の場合普通に営業日5日
          val vWeekTotalTime = DAY_WORK_TIME * REAL_WORK_DAY
          weekData = weekData :+
            WeekData(szYobi,1,DETECT_MONTH + "-01",vWeekLastDay,TOTAL_WORK_DAY,REAL_WORK_DAY,vWeekTotalTime)
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
            WeekData(szYobi,1,DETECT_MONTH + "-01",vWeekLastDay,vTermDay,vRealWorkDay,vWeekTotalTime)
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
          val szYobi = vTargetDate + "日("+ this.getYobi(vTargetDate) + ")"
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
        val vTemp= vWeekBeforeFirstDay.splitAt(8)
        val intWeekFirstDay =vTemp._2.toInt
        val szYobi = intWeekFirstDay + "日("+ this.getYobi(vWeekBeforeFirstDay) + ")"
        weekData = weekData :+
          WeekData(szYobi,iWeekIndex,vWeekBeforeFirstDay,vWeekLastDay,vTermDay,vRealWorkDay,vWeekTotalTime)
      }
      iWeekIndex = iWeekIndex + 1; // 週目カウントを増加
    }
    return weekData
  }
  /** 　検索側データ取得 */
  def getCalendarData(): List[WeekData] = {

    val weekData = if (this.CALENDAR_TYPE == "今月のみ") {
      this.getOnlyThisMonthData() // 今月のみの場合
    } else {
      this.getMonthData()
    }

    System.out.println("=========[" +DETECT_MONTH+"]=======")
    weekData.map{ v=>
      System.out.println("================")
      System.out.println("曜日：" +v.szYobi)
      System.out.println("週目：" +v.iNum)
      System.out.println("週最初日：" +v.iWeekStartDay)
      System.out.println("週最終日：" +v.iWeekEndDay)
      System.out.println("週全日：" +v.iWeekTotalWorkDay)
      System.out.println("週働く日：" +v.iWeekRealWorkDay)
      System.out.println("週働く時間：" +v.iWeekTotalTime)
      System.out.println("================")
    }
    return weekData
  }


  /** csv出力 */
  def csvExport = SecuredAction { implicit request =>
    System.out.println("start csvExport:")
    val placeId = super.getCurrentPlaceId
    val calendarList =  this.getCalendarData()
    val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)

    try{
      // csv ロジック
      val file = new File("temp_export.csv")
      if (file.exists()) {
        file.delete()
      }
      val os = new FileOutputStream(file)
      val pw = new PrintWriter(new OutputStreamWriter(os, "SJIS"));
      pw.println(CSV_HEAD)
      pw.print(s"${DETECT_MONTH} 作業車稼働状況分析")
      pw.println("")
      pw.print("作業車,")
      calendarList.foreach { calendar =>
        pw.print(s"${calendar.szYobi}の週," +
          s"実${calendar.iWeekRealWorkDay}/${calendar.iWeekTotalWorkDay}日,"
        )
      }
      pw.println("")
      pw.print("名称,")
      calendarList.foreach { calendar =>
        pw.print(s"稼働率," +
          s"予約/稼働,"
        )
      }
      pw.println("")
      logItemAllList.foreach { item =>
        pw.print(s"${item.last.itemName},")
        item.map{ v =>
          pw.print(s"${v.operatingRate}," +
            s"${v.reserveOperatingRate},"
          )
        }
        pw.println("")
      }
      pw.close()
      Ok.sendFile(content = file, fileName = _ => "MONTH_MOVEMENT_CAR_V1.csv")
    }catch {
      case e: Exception =>
        Redirect(routes.MovementCar.index())
          .flashing(ERROR_MSG_KEY -> Messages("error.analysis.movementCar.csvExport"))
    }
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

    val calendarList =  this.getCalendarData()
    val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)

    Ok(views.html.analysis.movementCar(logItemAllList,calendarList,DETECT_MONTH,TOTAL_LENGTH))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 初期化
    init();
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // DB探索になる今月に関するデータをセット
    val calendarList =  this.getCalendarData()
    val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)
    Ok(views.html.analysis.movementCar(logItemAllList,calendarList,DETECT_MONTH,TOTAL_LENGTH))
  }

}
