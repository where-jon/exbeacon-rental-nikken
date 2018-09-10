package controllers.site

import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.errors
import controllers.site
import controllers.{BaseController, BeaconService}
import models.{ItemOtherReserveData, _}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * その他仮設材予約画面
  *
  *
  */


@Singleton
class ItemOtherReserve @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , carDAO: models.itemCarDAO
  , otherDAO: models.itemOtherDAO
  , companyDAO: models.companyDAO
  , beaconService: BeaconService
  , floorDAO: models.floorDAO
  , btxDAO: models.btxDAO
  ,reserveMasterDAO: models.ReserveMasterDAO
  , itemTypeDAO: models.ItemTypeDAO
  , workTypeDAO: models.WorkTypeDAO
  ) extends BaseController with I18nSupport {

   /*検索用*/
  var ITEM_TYPE_FILTER = 0;
  var RESERVE_START_DATE = "";
  var RESERVE_END_DATE = "";
  var WORK_TYPE_FILTER = "";

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


    val itemOtherSearchForm = Form(mapping(
    "itemTypeId" ->number,
    "workTypeName" -> text,
    "inputStartDate" -> text,
    "inputEndDate" -> text
  )(ItemOtherSearchData.apply)(ItemOtherSearchData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    WORK_TYPE_FILTER = "終日"
    RESERVE_START_DATE = ""
    RESERVE_START_DATE = ""

    COMPANY_NAME_FILTER = ""
    FLOOR_NAME_FILTER = ""
  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemOtherInfo(_placeId);
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
    val itemOtherForm = Form(mapping(
      "itemTypeId" -> number,
      "workTypeName" -> text.verifying(Messages("error.site.itemReserve.form.workType"), { workTypeName => !workTypeName.isEmpty() }),
      "inputStartDate" -> text.verifying(Messages("error.site.itemReserve.form.reserveStartDate"), { inputStartDate => !inputStartDate.isEmpty() }),
      "inputEndDate" -> text.verifying(Messages("error.site.itemReserve.form.reserveEndDate"), { inputEndDate => !inputEndDate.isEmpty() }),
      "companyName" -> text.verifying(Messages("error.site.itemReserve.form.company"), { companyName => !companyName.isEmpty() }),
      "floorName" -> text.verifying(Messages("error.site.itemReserve.form.floor"), { floorName => !floorName.isEmpty() }),
      "itemId" -> list(number.verifying(Messages("error.site.itemReserve.form.id"), { itemId => itemId != null })),
      "itemTypeIdList" -> list(number.verifying(Messages("error.site.itemReserve.form.type"), { itemTypeIdList => itemTypeIdList != null })),
      "checkVal" -> list(number.verifying(Messages("error.site.itemReserve.form.select"), { checkVal => checkVal != null }))
    )(ItemOtherReserveData.apply)(ItemOtherReserveData.unapply))

    val form = itemOtherForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemOtherReserve.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      val ItemOtherReserveData = form.get
      if(ItemOtherReserveData.checkVal.zipWithIndex.length > 0){
        val vCurrentTimeCheck =
          beaconService.currentTimeReserveCheck(ItemOtherReserveData.inputStartDate,ItemOtherReserveData.workTypeName)
        if(vCurrentTimeCheck == "OK"){
          var setData = List[ReserveItem]()
          val vCompanyId = companyNameList.filter(_.companyName == ItemOtherReserveData.companyName).last.companyId
          val vFloorId = floorNameList.filter(_.floor_name == ItemOtherReserveData.floorName).last.floor_Id
          val vWorkTypeId = workTypeList.filter(_.work_type_name == ItemOtherReserveData.workTypeName).last.work_type_id
          val vReserveStartDate = ItemOtherReserveData.inputStartDate
          val vReserveEndDate = ItemOtherReserveData.inputEndDate
          var idListData = List[Int]()
          var idTypeListData = List[Int]()
          ItemOtherReserveData.itemId.zipWithIndex.map { case (itemId, i) =>
            ItemOtherReserveData.checkVal.zipWithIndex.map { case (check, j) =>
              if(i == check){
                val vItemTypeId = ItemOtherReserveData.itemTypeIdList(i)
                idListData = idListData :+itemId
                idTypeListData = idTypeListData :+vItemTypeId
                setData = setData :+ ReserveItem(vItemTypeId,itemId,vFloorId,placeId,vCompanyId,vReserveStartDate,vReserveEndDate,true,vWorkTypeId)
              }
            }
          }

          var errMsg = Seq[String]()
          // 検索ロジック追加あるかどうかを判断する
          val vAlerdyReserveData = reserveMasterDAO.selectOtherReserve(placeId,idListData,idTypeListData,vWorkTypeId,vReserveStartDate,vReserveEndDate)
          System.out.println("vCount :" + vAlerdyReserveData)
          if(vAlerdyReserveData.isEmpty) { // 予約されたものがない
            val result = otherDAO.reserveItemOther(setData)
            if (result == "success") {
              Redirect(routes.ItemOtherReserve.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.otherReserve.update"))
            }else {
              Redirect(routes.ItemOtherReserve.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.otherReserve.update" ))
            }
          }else{ // 予約されたものがある
            errMsg :+= Messages("error.site.itemOtherReserve.reserve")
            errMsg :+= Messages("error.site.itemOtherReserve.id" ,vAlerdyReserveData.last.txId)
            errMsg :+= Messages("error.site.itemOtherReserve.workType" ,vAlerdyReserveData.last.workTypeName)
            errMsg :+= Messages("error.site.itemOtherReserve.reserveDate"  ,vAlerdyReserveData.last.reserveStartDate,vAlerdyReserveData.last.reserveEndDate)
            errMsg :+= Messages("error.site.itemCarReserve.already" )

            Redirect(routes.ItemOtherReserve.index())
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          }
        }else{ // 現在時刻から予約可能かを判定でエラーの場合
          if(vCurrentTimeCheck == "当日"){
            val currentTime = new Date();
            val mSimpleDateFormatHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
            val mCurrentTime = mSimpleDateFormatHour.format(currentTime)
            var errMsg = Seq[String]()
            errMsg :+= vCurrentTimeCheck
            errMsg :+= Messages("error.site.reserve.overtime", mCurrentTime,ItemOtherReserveData.workTypeName)
            errMsg :+= Messages("error.site.reserve.overtime.define")
            Redirect(routes.ItemOtherReserve.index())
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          }else{
            Redirect(routes.ItemOtherReserve.index())
              .flashing(ERROR_MSG_KEY -> Messages(vCurrentTimeCheck))
          }
        }
      }else{
        Redirect(routes.ItemOtherReserve.index())
          .flashing(ERROR_MSG_KEY -> Messages("error.site.otherReserve.noselect"))
      }
    }
  }
  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 部署情報
    val otherFormSearchData = itemOtherSearchForm.bindFromRequest.get
    ITEM_TYPE_FILTER = otherFormSearchData.itemTypeId
    WORK_TYPE_FILTER = otherFormSearchData.workTypeName
    RESERVE_START_DATE = otherFormSearchData.inputStartDate
    RESERVE_END_DATE = otherFormSearchData.inputEndDate

    var dbDatas : Seq[OtherViewer] = null;
    // dbデータ取得
    if(RESERVE_START_DATE!="" || RESERVE_END_DATE!=""){
      dbDatas = otherDAO.selectOtherMasterSearch(placeId,ITEM_TYPE_FILTER,WORK_TYPE_FILTER,
        RESERVE_START_DATE,RESERVE_END_DATE,itemIdList)
      if( WORK_TYPE_FILTER != "終日" && WORK_TYPE_FILTER != ""){
        val vWorkTypeCountData = reserveMasterDAO.getOtherMasterWorkTypeCount(placeId,RESERVE_START_DATE,RESERVE_END_DATE,itemIdList)
        vWorkTypeCountData.map { v =>
          if(v.item_count>1){
            dbDatas = dbDatas.filter(_.item_other_id != v.item_id)
          }
        }
      }
    }else{
      dbDatas = otherDAO.selectOtherMasterReserve(placeId,itemIdList)
    }
    var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      otherListApi = otherListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }

    Ok(views.html.site.itemOtherReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_START_DATE,RESERVE_END_DATE
      ,otherListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
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
      val dbDatas = otherDAO.selectOtherMasterReserve(placeId,itemIdList)
      val otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)
      if(otherListApi!=null){
        Ok(views.html.site.itemOtherReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_START_DATE,RESERVE_END_DATE
          ,otherListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
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
