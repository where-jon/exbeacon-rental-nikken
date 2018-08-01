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
  * 作業車・立馬予約画面
  *
  *
  */


@Singleton
class UnDetected @Inject()(config: Configuration
                               , val silhouette: Silhouette[MyEnv]
                               , val messagesApi: MessagesApi
                               , carDAO: models.itemCarDAO
                               , companyDAO: models.companyDAO
                               , beaconService: BeaconService
                               , floorDAO: models.floorDAO
                               , btxDAO: models.btxDAO
                               , reserveMasterDAO: models.ReserveMasterDAO
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
  val itemCarForm = Form(mapping(
    "itemTypeId" -> number,
    "workTypeName" -> text.verifying("作業期間 未設定", { workTypeName => !workTypeName.isEmpty() }),
    "inputDate" -> text.verifying("予約日 未設定", { inputDate => !inputDate.isEmpty() }),
    "companyName" -> text.verifying("予約会社 未設定", { companyName => !companyName.isEmpty() }),
    "floorName" -> text.verifying("予約フロア 未設定", { floorName => !floorName.isEmpty() }),
    "itemId" -> list(number.verifying("仮設材ID 異常", { itemId => itemId != null })),
    "itemTypeIdList" -> list(number.verifying("仮設材種別 異常", { itemTypeIdList => itemTypeIdList != null })),
    "checkVal" -> list(number.verifying("選択", { checkVal => checkVal != null }))
  )(ItemCarReserveData.apply)(ItemCarReserveData.unapply))


  val itemCarSearchForm = Form(mapping(
    "itemTypeId" ->number,
    "workTypeName" -> text,
    "inputDate" -> text
  )(ItemCarSearchData.apply)(ItemCarSearchData.unapply))

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

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 部署情報
    val carFormSearchData = itemCarSearchForm.bindFromRequest.get
    ITEM_TYPE_FILTER = carFormSearchData.itemTypeId
    WORK_TYPE_FILTER = carFormSearchData.workTypeName
    RESERVE_DATE = carFormSearchData.inputDate

    var dbDatas : Seq[CarViewer] = null;
    // dbデータ取得
    if(RESERVE_DATE!=""){
      dbDatas = carDAO.selectCarMasterSearch(placeId,ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE,itemIdList)
      // 午前午後両方予約の場合
      if( WORK_TYPE_FILTER != "終日" && WORK_TYPE_FILTER != ""){
        val vWorkTypeCountData = reserveMasterDAO.getCarMasterWorkTypeCount(placeId,RESERVE_DATE,itemIdList)
        vWorkTypeCountData.map { v =>
          if(v.item_count>1){
            dbDatas = dbDatas.filter(_.item_car_id != v.item_id)
          }
        }
      }
    }else{
      dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
    }
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      carListApi = carListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }


    Ok(views.html.site.unDetected(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
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
    val dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    // 全体から空いてるものだけ表示する。
    //carListApi = carListApi.filter(_.reserve_id == -1)

    System.out.println("carListApi:" + carListApi.length)
    Ok(views.html.site.unDetected(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
      ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
  }

}
