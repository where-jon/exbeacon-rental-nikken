package controllers.analysis

import java.io.{File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, site}
import javax.inject.{Inject, Singleton}
import models._
import models.manage.MovementCarData
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import utils.silhouette.MyEnv


/**
  * 作業車稼働状況分析画面
  *
  *
  */

@Singleton
class MovementCarController @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
, carDAO: models.manage.ItemCarDAO
, companyDAO: models.manage.CompanyDAO
, beaconService: BeaconService
, floorDAO: models.system.floorDAO
, btxDAO: models.btxDAO
, itemlogDAO: ItemLogDAO
, itemTypeDAO: models.ItemTypeDAO
, workTypeDAO: models.WorkTypeDAO
, calendarDAO: models.LogCalendarDAO
, pagiNationHelper: common.PagiNation
) extends BaseController with I18nSupport {

   /*検索用*/
  var DETECT_MONTH = "";
  val TOTAL_WORK_DAY = 7;
  val REAL_WORK_DAY = 5;
  val DAY_WORK_TIME = 6;
  val HOUR_MINUTE = 60;
  val FIX_COLUMN =2
  var BATCH_INTERVAL_MINUTE = 60; //ログ検知インタバル初期値60分 daidan30分

  val CALENDAR_TYPE = "今月のみ";
  //val CALENDAR_TYPE = "今月以外";

  var DETECT_MONTH_DAY = "";
  var NOW_DATE = "";
  var TOTAL_LENGTH = 0;

  val movementCarSearchForm = Form(mapping(
    "inputDate" -> text.verifying(Messages("error.analysis.movementCar.search.date.empty"), {!_.isEmpty})
  )(MovementCarSearchData.apply)(MovementCarSearchData.unapply))

  /*登録用*/
  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id

  /*csv用*/
  var CSV_HEAD = ""

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
    BATCH_INTERVAL_MINUTE = config.getInt("web.positioning.countMinute").get
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
  def getDetectedCount(detectedCount:Integer, weekTotalTime:Integer,bRealWorkDayCheck:Boolean) : Float = {
    if(detectedCount == 0 || detectedCount == -1){
      0
    }else{
      val vCountResult = (detectedCount * BATCH_INTERVAL_MINUTE) / (weekTotalTime * HOUR_MINUTE).toFloat * 100
      if(!bRealWorkDayCheck)
        Math.round(vCountResult*10)/10.0.toFloat + 100 // 週末判定で入って働いたら100%すでに超え
      else
        Math.round(vCountResult*10)/10.0.toFloat // 小数点2位以下を四捨五
    }
  }


  /** 　item_logテーブルデータ取得 */
  def getAllItemLogData(placeId:Int,itemIdList :Seq[Int],calendarList:List[WeekData]) : Seq[List[WorkRate]] = {

    // ①itemCarテーブルlistからsqlを組むplaceId
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)  // 作業車のみ
    // 共通ページングからデータ取得
    val logItemList = pagiNationHelper.getMovementPageData(dbDatas)

    // ②. ①から取得したデータへgetCalndarDataを含む
    val allData = logItemList.zipWithIndex.map { case (car, i) =>
      calendarList.zipWithIndex.map{ case (calendar, i) =>

        // 検知フラグがtrue
        val getWorkFlgSqlData =itemlogDAO.selectWorkingOn(true,car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
        val vWofkFlgDetectCount = getWorkFlgSqlData.last.detected_count
//        if(vWofkFlgDetectCount>0){
//          System.out.println(vWofkFlgDetectCount)
//        }
        var bRealWorkDayCheck = true
        // 実際働く時間（土、日だけの場合もある）
        val vRealWorkTime = if(calendar.iWeekRealWorkDay == 0 ){
          bRealWorkDayCheck = false
          calendar.iWeekTotalWorkDay * DAY_WORK_TIME // 実際残り日（土日最大二日）から動いたら判定に入れる
        }else {
          calendar.iWeekTotalTime
        }
        // 週末があるため実際
        val vOperatingRate = this.getDetectedCount(vWofkFlgDetectCount,vRealWorkTime,bRealWorkDayCheck)
        // 予約ともに検知フラグがtrue
        val getReserveWorkFlgSqlData =itemlogDAO.selectReserveAndWorkingOn(true,true,car.item_car_id,placeId,calendar.iWeekStartDay,calendar.iWeekEndDay,itemIdList)
        val vReserveWofkFlgDetectCount = getReserveWorkFlgSqlData.last.detected_count
        val vReserveOperatingRate = this.getDetectedCount(vReserveWofkFlgDetectCount,vRealWorkTime,bRealWorkDayCheck)

        WorkRate(car.item_car_id,car.item_car_btx_id,car.item_car_key_btx_id,car.item_car_no,car.item_car_name,vOperatingRate,vReserveOperatingRate,vWofkFlgDetectCount,vReserveWofkFlgDetectCount)

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
          val szYobi = vTargetDate + "日("+ calendarDAO.getYobi(vTargetDate) + ")"
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
        val szYobi = intWeekFirstDay + "日("+ calendarDAO.getYobi(vWeekFirstDay) + ")"
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
          "1日("+ calendarDAO.getYobi(DETECT_MONTH + "-01") + ")"

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
          val szYobi = vTargetDate + "日("+ calendarDAO.getYobi(vTargetDate) + ")"
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
        val szYobi = intWeekFirstDay + "日("+ calendarDAO.getYobi(vWeekBeforeFirstDay) + ")"
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

//    System.out.println("=========[" +DETECT_MONTH+"]=======")
//    weekData.map{ v=>
//      System.out.println("================")
//      System.out.println("曜日：" +v.szYobi)
//      System.out.println("週目：" +v.iNum)
//      System.out.println("週最初日：" +v.iWeekStartDay)
//      System.out.println("週最終日：" +v.iWeekEndDay)
//      System.out.println("週全日：" +v.iWeekTotalWorkDay)
//      System.out.println("週働く日：" +v.iWeekRealWorkDay)
//      System.out.println("週働く時間：" +v.iWeekTotalTime)
//      System.out.println("================")
//    }
    return weekData
  }


  /** csv出力 */
  def csvExport(page:Int) = SecuredAction { implicit request =>

    /*転送form*/
    val movementCarForm = Form(mapping(
      "itemDataList" -> list(text.verifying(Messages("error.analysis.movementCar.form.error"), { itemDataList => !itemDataList.isEmpty() }))
    )(MovementCarData.apply)(MovementCarData.unapply))
    val form = movementCarForm.bindFromRequest
    //System.out.println("start csvExport:")
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.MovementCarController.index(1)).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))

    }else {
      val placeId = super.getCurrentPlaceId
      val calendarList =  this.getCalendarData()
      val brLength = ( calendarList.length * FIX_COLUMN ) + FIX_COLUMN
      val movementData = form.get
      //val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)
      try{
        // csv ロジック
        val file = new File("/tmp/temp_export.csv")
        if (file.exists()) {
          file.delete()
        }
        val os = new FileOutputStream(file)
        val pw = new PrintWriter(new OutputStreamWriter(os, "SJIS"));
        CSV_HEAD = DETECT_MONTH +"_MOVEMENT_CAR_PAGE_" + page
        pw.println(CSV_HEAD)
        pw.print(s"${DETECT_MONTH} 作業車稼働状況分析")
        pw.println("")
        pw.print("作業車,")
        pw.print("作業車,")
              calendarList.foreach { calendar =>
                pw.print(s"${calendar.szYobi}の週," +
                  s"実${calendar.iWeekRealWorkDay}/${calendar.iWeekTotalWorkDay}日,"
                )
              }
        pw.println("")
        pw.print("番号,")
        pw.print("名称,")
              calendarList.foreach { calendar =>
                pw.print(s"稼働率," +
                  s"予約/稼働,"
                )
              }
        pw.println("")

        movementData.itemDataList.zipWithIndex.map { case (itemData, index) =>
          if(index!=0 && index % brLength == 0){
            pw.println("")
          }
          pw.print(s"${itemData},")
        }
        pw.close()
        Ok(Json.toJson("ok"))
        Ok.sendFile(content = file, fileName = _ => CSV_HEAD +".csv")
      }catch {
        case e: Exception =>
          Redirect(routes.MovementCarController.index(1))
            .flashing(ERROR_MSG_KEY -> Messages("error.analysis.movementCar.csvExport"))
      }

    }
  }


  /** 　検索ロジック */
  def search(page:Int) = SecuredAction { implicit request =>
    movementCarSearchForm.bindFromRequest.fold(
      formWithErrors =>
        Redirect(routes.MovementCarController.index(1))
            .flashing(ERROR_MSG_KEY -> Messages("error.analysis.movementCar.search.date.empty")),
      searchForm => {
        val placeId = super.getCurrentPlaceId
        pagiNationHelper.PAGE = page
        //検索側データ取得
        getSearchData(placeId)
        // 検索情報
        DETECT_MONTH = searchForm.inputDate
        val calendarList =  this.getCalendarData()
        val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)

        Ok(views.html.analysis.movementCar(logItemAllList,calendarList,DETECT_MONTH,TOTAL_LENGTH, pagiNationHelper.PAGE, pagiNationHelper.MAX_PAGE))
      }
    )
  }

  /** 　検索ロジック ページネーション用 */
  def searchPaging(page:Int) = SecuredAction { implicit request =>
    val placeId = super.getCurrentPlaceId
    pagiNationHelper.PAGE = page
    //検索側データ取得
    getSearchData(placeId)
    // 検索情報
    val calendarList =  this.getCalendarData()
    val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)

    Ok(views.html.analysis.movementCar(logItemAllList,calendarList,DETECT_MONTH,TOTAL_LENGTH, pagiNationHelper.PAGE, pagiNationHelper.MAX_PAGE))
  }

  /** 初期表示 */
  def index(page:Int) = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 初期化
      init();
      val placeId = super.getCurrentPlaceId
      //検索側データ取得
      getSearchData(placeId)

      // DB探索になる今月に関するデータをセット
      pagiNationHelper.PAGE = page
      val calendarList =  this.getCalendarData()
      val logItemAllList =  getAllItemLogData(placeId,itemIdList,calendarList)
      Ok(views.html.analysis.movementCar(logItemAllList,calendarList,DETECT_MONTH,TOTAL_LENGTH, pagiNationHelper.PAGE, pagiNationHelper.MAX_PAGE))
    }else{
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

}
