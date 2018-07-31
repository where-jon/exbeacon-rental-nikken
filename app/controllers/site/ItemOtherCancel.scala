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
  * その他仮設材予約取消画面
  *
  *
  */

@Singleton
class ItemOtherCancel @Inject()(config: Configuration
                               , val silhouette: Silhouette[MyEnv]
                               , val messagesApi: MessagesApi
                                , otherDAO: models.itemOtherDAO
                               , companyDAO: models.companyDAO
                               , beaconService: BeaconService
                               , floorDAO: models.floorDAO
                               , btxDAO: models.btxDAO
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
  val itemOtherCancelForm = Form(mapping(
    "itemTypeIdList" -> list(number.verifying("仮設材TypeIDが異常", { itemTypeIdList => itemTypeIdList != null })),
    "itemId" -> list(number.verifying("仮設材IDが異常", { itemId => itemId != null })),
    "checkVal" -> list(number.verifying("checkVal", { checkVal => checkVal != null }))
  )(ItemOtherCancelData.apply)(ItemOtherCancelData.unapply))


    val itemOtherSearchForm = Form(mapping(
    "itemTypeId" ->number,
    "workTypeName" -> text,
     "companyName" -> text,
      "inputStartDate" -> text,
      "inputEndDate" -> text
  )(ItemOtherCancelSearchData.apply)(ItemOtherCancelSearchData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    WORK_TYPE_FILTER = ""
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

  /** 　予約取消ロジック */
  def cancel = SecuredAction { implicit request =>
    // dbデータ取得
    val placeId = super.getCurrentPlaceId
    getSearchData(placeId)
    val dbDatas = otherDAO.selectOtherMasterReserve(placeId,itemIdList)
    var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)
    itemOtherCancelForm.bindFromRequest.fold(
      formWithErrors =>
      Redirect(routes.ItemOtherCancel.index())
          .flashing(ERROR_MSG_KEY -> Messages(formWithErrors.errors.map(_.message +"<br>").mkString("\n"))),

      ItemOtherReserveData => {
        if(ItemOtherReserveData.checkVal.zipWithIndex.length > 0){
            var setData = List[CancelItem]()

          ItemOtherReserveData.itemId.zipWithIndex.map { case (itemId, i) =>
            ItemOtherReserveData.checkVal.zipWithIndex.map { case (check, j) =>
                if(i == check){
                  val vItemTypeId = ItemOtherReserveData.itemTypeIdList(i)
                  setData = setData :+ CancelItem(vItemTypeId,itemId,placeId,true)
                }
              }
            }
            val result = otherDAO.cancelItemOther(setData)
            if (result == "success") {
              Redirect(routes.ItemOtherCancel.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.otherCancel.cancel"))
            }else {
              Redirect(routes.ItemOtherCancel.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.otherCancel.cancel"))
            }
        }else{
          Redirect(routes.ItemOtherCancel.index())
            .flashing(ERROR_MSG_KEY -> Messages("error.site.otherCancel.noselect"))
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
    COMPANY_NAME_FILTER = otherFormSearchData.companyName
    WORK_TYPE_FILTER = otherFormSearchData.workTypeName
    RESERVE_START_DATE = otherFormSearchData.inputStartDate
    RESERVE_END_DATE = otherFormSearchData.inputEndDate

    var dbDatas : Seq[OtherViewer] = null;
    if (RESERVE_START_DATE != "" && RESERVE_END_DATE != "") {
      dbDatas = otherDAO.selectOtherMasterCancelSearch(placeId,itemIdList,RESERVE_START_DATE,RESERVE_END_DATE)
    }else {
      dbDatas = otherDAO.selectOtherMasterCancel(placeId,itemIdList)
    }

    var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      otherListApi = otherListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }
    if (WORK_TYPE_FILTER != "") {
      otherListApi = otherListApi.filter(_.work_type_name == WORK_TYPE_FILTER)
    }
    if (COMPANY_NAME_FILTER != "") {
      otherListApi = otherListApi.filter(_.company_name == COMPANY_NAME_FILTER)
    }


    Ok(views.html.site.itemOtherCancel(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_START_DATE,RESERVE_END_DATE
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
      val dbDatas = otherDAO.selectOtherMasterCancel(placeId,itemIdList)
      var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)


      System.out.println("otherListApi:" + otherListApi.length)
      Ok(views.html.site.itemOtherCancel(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_START_DATE,RESERVE_END_DATE
        ,otherListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
  }

}