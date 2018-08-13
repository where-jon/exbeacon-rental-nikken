package controllers.site

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

  /*転送form*/
  val itemOtherForm = Form(mapping(
    "itemTypeId" -> number,
    "workTypeName" -> text.verifying("作業期間 未設定", { workTypeName => !workTypeName.isEmpty() }),
    "inputStartDate" -> text.verifying("予約最初日 未設定", { inputStartDate => !inputStartDate.isEmpty() }),
    "inputEndDate" -> text.verifying("予約最終日 未設定", { inputEndDate => !inputEndDate.isEmpty() }),
    "companyName" -> text.verifying("予約会社 未設定", { companyName => !companyName.isEmpty() }),
    "floorName" -> text.verifying("予約フロア 未設定", { floorName => !floorName.isEmpty() }),
    "itemId" -> list(number.verifying("仮設材ID 異常", { itemId => itemId != null })),
    "itemTypeIdList" -> list(number.verifying("仮設材種別 異常", { itemTypeIdList => itemTypeIdList != null })),
    "checkVal" -> list(number.verifying("選択", { checkVal => checkVal != null }))
  )(ItemOtherReserveData.apply)(ItemOtherReserveData.unapply))


    val itemOtherSearchForm = Form(mapping(
    "itemTypeId" ->number,
    "workTypeName" -> text,
    "inputStartDate" -> text,
    "inputEndDate" -> text
  )(ItemOtherSearchData.apply)(ItemOtherSearchData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    WORK_TYPE_FILTER = ""
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

    itemOtherForm.bindFromRequest.fold(
      formWithErrors =>
      Redirect(routes.ItemOtherReserve.index())
          .flashing(ERROR_MSG_KEY -> Messages(formWithErrors.errors.map(_.message +"<br>").mkString("\n"))),

      ItemOtherReserveData => {
        if(ItemOtherReserveData.inputStartDate > ItemOtherReserveData.inputEndDate){
          Redirect(routes.ItemOtherReserve.index())
            .flashing(ERROR_MSG_KEY -> Messages("予約最初日より予約最終日が前日になってます"))
        }else{
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
                Redirect(routes.ItemOtherReserve.index())
                  .flashing(ERROR_MSG_KEY -> Messages("その他仮設材予約に問題が発生しました。" + "<br>"
                    + "「Id」" + vAlerdyReserveData.last.itemId
                    + "「作業期間」" + ItemOtherReserveData.workTypeName
                    + "「予約日」" + vAlerdyReserveData.last.reserveStartDate
                    + " ~ " + vAlerdyReserveData.last.reserveEndDate
                    + "すでに予約されてます"))
              }
            }else{ // 現在時刻から予約可能かを判定でエラーの場合
              Redirect(routes.ItemOtherReserve.index())
                .flashing(ERROR_MSG_KEY -> Messages(vCurrentTimeCheck))
            }
          }else{
            Redirect(routes.ItemOtherReserve.index())
              .flashing(ERROR_MSG_KEY -> Messages("予約対象未選択"))
          }
        }
      }
    )

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
    // 初期化
    init();
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // dbデータ取得
    val dbDatas = otherDAO.selectOtherMasterReserve(placeId,itemIdList)
    var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)

    // 全体から空いてるものだけ表示する。
    //otherListApi = otherListApi.filter(_.reserve_id == -1)
    System.out.println("otherListApi:" + otherListApi.length)
    Ok(views.html.site.itemOtherReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_START_DATE,RESERVE_END_DATE
      ,otherListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))

  }
}
