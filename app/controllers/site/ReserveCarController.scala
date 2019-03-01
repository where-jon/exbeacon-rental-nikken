package controllers.site

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
import models._
import models.manage._
import models.system.Floor
import play.api._
import play.api.data.Form
import play.api.data.Forms.{text, _}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv

import util.control.Breaks._

/**
  * 作業車・立馬予約画面
  *
  *
  */

@Singleton
class ReserveCarController @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
, carDAO: models.manage.ItemCarDAO
, companyDAO: models.manage.CompanyDAO
, beaconService: BeaconService
, floorDAO: models.system.floorDAO
, reserveMasterDAO: models.site.ReserveMasterDAO
, itemTypeDAO: models.manage.ItemTypeDAO
, workTypeDAO: models.WorkTypeDAO
, calendarDAO: models.LogCalendarDAO
) extends BaseController with I18nSupport {

   /*検索用*/
  var ITEM_TYPE_FILTER = 0;
  var ITEM_NAME_FILTER = "";
  var RESERVE_DATE = "";
  var WORK_TYPE_FILTER = "";
  var TERM_DAY = 7; //(当日からからスタートし+END_DATEの形 例END_DATE = 7-> 当日+7 = 8日間)
  var DETECT_DATE = "";

  /*登録用*/
  var COMPANY_NAME_FILTER = "";
  var FLOOR_NAME_FILTER = "";

  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id
  var companyNameList :Seq[Company] = null; // 業者
  var floorNameList :Seq[Floor] = null; // フロア
  var workTypeList :Seq[WorkType] = null; // 作業期間種別

  /*enum形*/
  val WORK_TYPE = WorkTypeEnum().map;

  var TOTAL_LENGTH = 0;

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    ITEM_NAME_FILTER = ""
    WORK_TYPE_FILTER = ""
    RESERVE_DATE = ""

    COMPANY_NAME_FILTER = ""
    FLOOR_NAME_FILTER = ""

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    DETECT_DATE = mTime
  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemCarInfo(_placeId);
    /*仮設材種別id取得*/
    itemIdList = itemTypeList.map{item => item.item_type_id}
    if(itemIdList.isEmpty){
      itemIdList = Seq(-1)
    }
    /*作業期間種別取得*/
    workTypeList = workTypeDAO.selectWorkInfo(_placeId);

    /*業者取得*/
    companyNameList = companyDAO.selectCompany(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);

  }

  /** 　予約ロジック */
  def reserve = SecuredAction { implicit request =>
    // dbデータ取得
    val placeId = super.getCurrentPlaceId
    getSearchData(placeId)

    /*転送form*/
    val itemCarForm = Form(mapping(
      "checkList" -> list(text.verifying(Messages("checkList error"), { checkList => !checkList.isEmpty() }))
      ,"itemId" -> list(number.verifying(Messages("error.site.itemReserve.form.id"), { itemId => itemId != null }))
      ,"itemTypeIdList" -> list(number.verifying(Messages("error.site.itemReserve.form.type"), { itemTypeIdList => itemTypeIdList != null }))
      ,"dayList" -> list(text.verifying(Messages("error.site.itemReserve.form.type"), { dayList => !dayList.isEmpty() }))
      ,"workTypeList" ->list(number.verifying(Messages("error.site.itemReserve.form.workType"), { workTypeList => workTypeList!= null}))
      ,"companyName" -> text.verifying(Messages("error.site.itemReserve.form.company"), { companyName => !companyName.isEmpty() })
      ,"floorName" -> text.verifying(Messages("error.site.itemReserve.form.floor"), { floorName => !floorName.isEmpty() })
    )(NewItemCarReserveData.apply)(NewItemCarReserveData.unapply))
//
    val form = itemCarForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ReserveCarController.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{

      var vTodayCheck = false
      var vAlreadyCheck = false
      var errMsg = Seq[String]()
      val ItemCarReserveData = form.get
      if(ItemCarReserveData.checkList.zipWithIndex.length > 0){
          var setData = List[ReserveItem]()
          val vCompanyId = companyNameList.filter(_.companyName == ItemCarReserveData.companyName).last.companyId
          val vFloorId = floorNameList.filter(_.floor_name == ItemCarReserveData.floorName).last.floor_Id
          //val vWorkTypeId = workTypeList.filter(_.work_type_name == ItemCarReserveData.workTypeList).last.work_type_id
          //ItemCarReserveData.itemId.zipWithIndex.foreach { case (itemId, i) =>
        breakable {
            ItemCarReserveData.checkList.zipWithIndex.foreach { case (check, j) =>
              val vIndex = check.toInt
              val vId = ItemCarReserveData.itemId(vIndex)
                val vReserveDate = ItemCarReserveData.dayList(vIndex)
                val vItemTypeId = ItemCarReserveData.itemTypeIdList(vIndex)
                val vWorkTypeId = ItemCarReserveData.workTypeList(vIndex)
                var idListData = List[Int]()
                var idTypeListData = List[Int]()
                idListData = idListData :+ vId
                idTypeListData = idTypeListData :+vItemTypeId
                var vTemp = ""
                if(vWorkTypeId == 1){
                  vTemp = "午前"
                }else if (vWorkTypeId == 2){
                  vTemp = "午後"
                }else if (vWorkTypeId == 3){
                  vTemp = "終日"
                }
                val vCurrentTimeCheck =
                beaconService.currentTimeReserveCheck(vReserveDate,vTemp)

                if(vCurrentTimeCheck == "OK"){  // 現在時刻から予約可能かを判定
                }else{
                  if(vCurrentTimeCheck == "当日"){
                    val currentTime = new Date();
                    val mSimpleDateFormatHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
                    val mCurrentTime = mSimpleDateFormatHour.format(currentTime)
                    errMsg :+= Messages("error.site.reserve.overtime", mCurrentTime,vTemp)
                    errMsg :+= Messages("error.site.reserve.overtime.define")
                  } else {
                    errMsg :+= Messages(vCurrentTimeCheck)
                  }
                  vTodayCheck = true
                  break
                }
              val vAlerdyReserveData = reserveMasterDAO.selectCarReserve(placeId,idListData,idTypeListData,vWorkTypeId,vReserveDate,vReserveDate)
              if(vAlerdyReserveData.isEmpty) { // 予約されたものがない
                setData = setData :+ ReserveItem(vItemTypeId,vId,vFloorId,placeId,vCompanyId,vReserveDate,vReserveDate,true,vWorkTypeId)
              }else{ // 予約されたものがある
                errMsg :+= Messages("error.site.itemCarReserve.reserve")
                errMsg :+= Messages("error.site.itemCarReserve.id" ,vAlerdyReserveData.last.txId)
                errMsg :+= Messages("error.site.itemCarReserve.workType" ,vAlerdyReserveData.last.workTypeName)
                errMsg :+= Messages("error.site.itemCarReserve.reserveDate"  ,vAlerdyReserveData.last.reserveStartDate)
                errMsg :+= Messages("error.site.itemCarReserve.already" )
                vAlreadyCheck = true
                break
              }
            }
        }
          if(vTodayCheck||vAlreadyCheck){
            System.out.println("---testCode .start---")
            System.out.println("errMsg::::" + errMsg)
            System.out.println("---testCode .end---")
            Redirect(routes.ReserveCarController.index())
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          }else{
            //　エラーチェック通ったら更新ロジック
            val result = carDAO.reserveItemCar(setData)
            if (result == "success") {
              Redirect(routes.ReserveCarController.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.carReserve.update"))
            }else {
              Redirect(routes.ReserveCarController.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.carReserve.update"))
            }
          }
      }else{
        Redirect(routes.ReserveCarController.index())
          .flashing(ERROR_MSG_KEY -> Messages("error.site.carReserve.noselect"))
      }

    }

  }
  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    val itemCarSearchForm = Form(mapping(
      "inputDate" -> text.verifying(Messages("error.site.reserveCar.inputDate.empty"), {inputDate => !inputDate.isEmpty() && inputDate.length > 0})
      ,"inputSearchDate" -> text.verifying(Messages("error.site.reserveCar.searchDate.over"), {inputSearchDate => !inputSearchDate.isEmpty() && inputSearchDate.toInt >=0 && inputSearchDate.toInt <=10})
      ,"inputName" -> text
      , "itemTypeId" -> number
      ,"floorName" -> text
    )(NewItemCarSearchData.apply)(NewItemCarSearchData.unapply))

    //System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    val form = itemCarSearchForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.ReserveCarController.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      // 部署情報
      val carFormSearchData = itemCarSearchForm.bindFromRequest.get
      ITEM_TYPE_FILTER = carFormSearchData.itemTypeId
      FLOOR_NAME_FILTER = carFormSearchData.floorName
      //ITEM_NAME_FILTER = carFormSearchData.inputName
      RESERVE_DATE = carFormSearchData.inputDate
      DETECT_DATE = RESERVE_DATE
      TERM_DAY = carFormSearchData.inputSearchDate.toInt

      // dbデータ取得
      // 設定日ロジック
      val arReserveDays = calendarDAO.selectGetDayOfWeek(RESERVE_DATE,TERM_DAY)
      TOTAL_LENGTH = arReserveDays.size
      val vStartDate = arReserveDays(0).getDay
      val vEndDate = arReserveDays((arReserveDays.size-1)).getDay
      val dbReserveDatas = carDAO.selectCarMasterCalendarType(placeId,itemIdList,vStartDate,vEndDate)
      var carListApi = beaconService.getItemCarReserveBeaconPosition(dbReserveDatas,arReserveDays,true,placeId)

      arReserveDays.zipWithIndex.foreach { case (reserveDay, index) =>
        val vTempYobi= calendarDAO.getYobi(reserveDay.getDay)
        reserveDay.getYobi = vTempYobi
      }
      // 名称検索
//      if(ITEM_NAME_FILTER!=""){
//        carListApi = carListApi.filter(_.item_car_name.contains(ITEM_NAME_FILTER))
//      }
      // 種別検索
      if (ITEM_TYPE_FILTER != 0) {
        carListApi = carListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
      }
      // フロア名検索
      if (FLOOR_NAME_FILTER != "") {
        carListApi = carListApi.filter(_.cur_pos_name == FLOOR_NAME_FILTER)
      }


      if(carListApi!=null){
        Ok(views.html.site.reserveCar(ITEM_TYPE_FILTER,FLOOR_NAME_FILTER,ITEM_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
          ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE,DETECT_DATE,TERM_DAY,arReserveDays,TOTAL_LENGTH))
      }else{
        // apiと登録データが違う場合
        Redirect(errors.routes.UnDetectedApi.indexSite)
          .flashing(ERROR_MSG_KEY -> Messages("error.unmatched.data"))
      }
    }

  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val placeId = super.getCurrentPlaceId
    if(beaconService.getCloudUrl(placeId)){
      // 初期化
      init();
      //検索側データ取得
      getSearchData(placeId)


      // dbデータ取得
      // 設定日ロジック
      val arReserveDays = calendarDAO.selectGetDayOfWeek(DETECT_DATE,TERM_DAY)
      TOTAL_LENGTH = arReserveDays.size
      val vStartDate = arReserveDays(0).getDay
      val vEndDate = arReserveDays((arReserveDays.size-1)).getDay
      val dbReserveDatas = carDAO.selectCarMasterCalendarType(placeId,itemIdList,vStartDate,vEndDate)
      val carListApi = beaconService.getItemCarReserveBeaconPosition(dbReserveDatas,arReserveDays,true,placeId)

      arReserveDays.zipWithIndex.foreach { case (reserveDay, index) =>
        val vTempYobi= calendarDAO.getYobi(reserveDay.getDay)
        reserveDay.getYobi = vTempYobi
      }

      if(carListApi!=null){
        Ok(views.html.site.reserveCar(ITEM_TYPE_FILTER,FLOOR_NAME_FILTER,ITEM_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
          ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE,DETECT_DATE,TERM_DAY,arReserveDays,TOTAL_LENGTH))
      }else{
        // apiと登録データが違う場合
        Redirect(errors.routes.UnDetectedApi.indexSite)
          .flashing(ERROR_MSG_KEY -> Messages("error.unmatched.data"))
      }
    }else{
      // apiデータがない場合
      Redirect(errors.routes.UnDetectedApi.indexSite)
        .flashing(ERROR_MSG_KEY -> Messages("error.undetected.api"))
    }
  }

}
