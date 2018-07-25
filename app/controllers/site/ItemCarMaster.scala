package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.BeaconService
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 作業車・立馬一覧クラス
  *
  *
  */


@Singleton
class ItemCarMaster @Inject()(config: Configuration
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

  var ITEM_TYPE_FILTER = 0;
  var COMPANY_NAME_FILTER = "";
  var FLOOR_NAME_FILTER = "";
  var WORK_TYPE_FILTER = "";

  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id
  var companyNameList :Seq[Company] = null; // 業者
  var floorNameList :Seq[Floor] = null; // フロア
  var workTypeList :Seq[WorkType] = null; // 作業期間種別

  /*enum形*/
  val WORK_TYPE = WorkTypeEnum().map;

  /*転送form*/
  val carForm = Form(mapping(
    "itemTypeId" -> number,
    "companyName" -> text,
    "floorName" -> text,
    "workTypeName" -> text
  )(ItemCarData.apply)(ItemCarData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    COMPANY_NAME_FILTER = ""
    WORK_TYPE_FILTER = ""
    FLOOR_NAME_FILTER = ""
  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemCarInfo(_placeId);
    /*仮設材種別id取得*/
    itemIdList = itemTypeList.map{item => item.item_type_id}
    /*業者取得*/
    companyNameList = companyDAO.selectCompany(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);
    /*作業期間種別取得*/
    workTypeList = workTypeDAO.selectWorkInfo(_placeId);
  }

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 部署情報
    val carFormData = carForm.bindFromRequest.get
    ITEM_TYPE_FILTER = carFormData.itemTypeId
    COMPANY_NAME_FILTER = carFormData.companyName
    WORK_TYPE_FILTER = carFormData.workTypeName
    FLOOR_NAME_FILTER = carFormData.floorName
    // dbデータ取得
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      carListApi = carListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }
    if (FLOOR_NAME_FILTER != "") {
      carListApi = carListApi.filter(_.cur_pos_name == FLOOR_NAME_FILTER)
    }
    if (COMPANY_NAME_FILTER != "") {
      carListApi = carListApi.filter(_.company_name == COMPANY_NAME_FILTER)
    }
    if (WORK_TYPE_FILTER != "") {
      carListApi = carListApi.filter(_.work_type_name == WORK_TYPE_FILTER)
    }

    Ok(views.html.site.itemCarMaster(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,FLOOR_NAME_FILTER,WORK_TYPE_FILTER
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
    val dbDatas = carDAO.selectCarMasterViewer(placeId,itemIdList)
    val carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    System.out.println("carListApi:" + carListApi.length)
    Ok(views.html.site.itemCarMaster(ITEM_TYPE_FILTER, COMPANY_NAME_FILTER,FLOOR_NAME_FILTER,WORK_TYPE_FILTER
      ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
  }

}
