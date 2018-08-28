package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv

/*作業車・立馬予約取消用クラス*/
case class CancelItem(
 item_type_id :Int,
 item_id :Int,
 place_id :Int,
 active_flg:Boolean
)

/**
  * 作業車・立馬取消画面
  *
  *
  */

@Singleton
class ItemCarCancel @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
, carDAO: models.itemCarDAO
, companyDAO: models.companyDAO
, beaconService: BeaconService
, floorDAO: models.floorDAO
, btxDAO: models.btxDAO
, itemTypeDAO: models.ItemTypeDAO
, workTypeDAO: models.WorkTypeDAO
) extends BaseController with I18nSupport {

   /*検索用*/
  var ITEM_TYPE_FILTER = 0;
  var RESERVE_DATE = "";
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
  val itemCarCancelForm = Form(mapping(
    "itemTypeIdList" -> list(number.verifying("仮設材TypeIDが異常", { itemTypeIdList => itemTypeIdList != null })),
    "itemId" -> list(number.verifying("仮設材IDが異常", { itemId => itemId != null })),
    "workTypeNameList" -> list(text.verifying("期間異常", { workTypeNameList => !workTypeNameList.isEmpty})),
    "reserveStartDateList" -> list(text.verifying("予約期間異常", { reserveStartDateList => !reserveStartDateList.isEmpty})),
    "checkVal" -> list(number.verifying("checkVal", { checkVal => checkVal != null }))
  )(ItemCarCancelData.apply)(ItemCarCancelData.unapply))


    val itemCarSearchForm = Form(mapping(
    "itemTypeId" ->number,
    "workTypeName" -> text,
     "companyName" -> text,
    "inputDate" -> text
  )(ItemCarCancelSearchData.apply)(ItemCarCancelSearchData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    WORK_TYPE_FILTER = ""
    RESERVE_DATE = ""

    COMPANY_NAME_FILTER = ""
    FLOOR_NAME_FILTER = ""
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

  /** 　予約取消ロジック */
  def cancel = SecuredAction { implicit request =>
    // dbデータ取得
    val placeId = super.getCurrentPlaceId
    getSearchData(placeId)
    val dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    //val carFormData = itemCarForm.bindFromRequest.get
    itemCarCancelForm.bindFromRequest.fold(
      formWithErrors =>
      Redirect(routes.ItemCarCancel.index())
          .flashing(ERROR_MSG_KEY -> Messages(formWithErrors.errors.map(_.message +"<br>").mkString("\n"))),

      ItemCarReserveData => {
        if(ItemCarReserveData.checkVal.zipWithIndex.length > 0){
          var setData = List[CancelItem]()
          var vCancelCheck = "OK"
          ItemCarReserveData.itemId.zipWithIndex.map { case (itemId, i) =>
            ItemCarReserveData.checkVal.zipWithIndex.map { case (check, j) =>
                if(i == check){
                  val vItemTypeId = ItemCarReserveData.itemTypeIdList(i)
                  val vReserveStartDate = ItemCarReserveData.reserveStartDateList(i)
                  val vWorkTypeName = ItemCarReserveData.workTypeNameList(i)
                  val vCheckValue = beaconService.currentTimeCancelCheck(vReserveStartDate,vWorkTypeName)
                  if(vCheckValue != "OK"){
                    vCancelCheck = vCheckValue
                  }
                  setData = setData :+ CancelItem(vItemTypeId,itemId,placeId,true)
                }
              }
            }
          if(vCancelCheck == "OK"){ //　現在時刻から予約取消可能かを判定
            val result = carDAO.cancelItemCar(setData)
            if (result == "success") {
              Redirect(routes.ItemCarCancel.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.carCancel.cancel"))
            }else {
              Redirect(routes.ItemCarCancel.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.carCancel.cancel"))
            }
          }else{  // 現在時刻から予約取消可能かを判定でエラーの場合
            Redirect(routes.ItemCarCancel.index())
              .flashing(ERROR_MSG_KEY -> Messages(vCancelCheck))
          }
        }else{
          Redirect(routes.ItemCarCancel.index())
            .flashing(ERROR_MSG_KEY -> Messages("error.site.carCancel.noselect"))
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
    val carFormSearchData = itemCarSearchForm.bindFromRequest.get
    ITEM_TYPE_FILTER = carFormSearchData.itemTypeId
    COMPANY_NAME_FILTER = carFormSearchData.companyName
    WORK_TYPE_FILTER = carFormSearchData.workTypeName
    RESERVE_DATE = carFormSearchData.inputDate

    var dbDatas : Seq[CarViewer] = null;
    dbDatas = carDAO.selectCarMasterCancel(placeId,itemIdList)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    if (ITEM_TYPE_FILTER != 0) {
      carListApi = carListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }
    if (WORK_TYPE_FILTER != "") {
      carListApi = carListApi.filter(_.work_type_name == WORK_TYPE_FILTER)
    }
    if (COMPANY_NAME_FILTER != "") {
      carListApi = carListApi.filter(_.company_name == COMPANY_NAME_FILTER)
    }
    if (RESERVE_DATE != "") {
      carListApi = carListApi.filter(_.reserve_start_date == RESERVE_DATE)
    }


    Ok(views.html.site.itemCarCancel(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
      ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
      // 初期化
      init();
      val placeId = super.getCurrentPlaceId
      //検索側データ取得
      getSearchData(placeId)

      // dbデータ取得
      val dbDatas = carDAO.selectCarMasterCancel(placeId,itemIdList)
      var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    if(carListApi!=null){
      Ok(views.html.site.itemCarCancel(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
        ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
    }else{
      // apiデータがない場合
      Redirect(errors.routes.UnDetectedApi.indexSite)
        .flashing(ERROR_MSG_KEY -> Messages("error.undetected.api"))
    }

  }

}
