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
class ItemCarReserve @Inject()(config: Configuration
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
  var companyNameList :Seq[Company] = null; // 業者
  var floorNameList :Seq[Floor] = null; // フロア
  var workTypeList :Seq[WorkType] = null; // 作業期間種別

  /*enum形*/
  val WORK_TYPE = WorkTypeEnum().map;

  /*転送form*/
  val carForm = Form(mapping(
    "itemTypeId" -> number,
    "workTypeName" -> text,
    "inputDate" -> text
  )(ItemCarReserveData.apply)(ItemCarReserveData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    WORK_TYPE_FILTER = ""
    RESERVE_DATE = ""
  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemCarInfo(_placeId);
    /*作業期間種別取得*/
    workTypeList = workTypeDAO.selectWorkInfo(_placeId);

    /*業者取得*/
    companyNameList = companyDAO.selectCompany(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);
  }

  /** 　予約ロジック */
  def update = SecuredAction { implicit request =>

    Redirect(routes.ItemCarReserve.index())
      .flashing(SUCCESS_MSG_KEY -> Messages("success.site.carReserve.update"))
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
    WORK_TYPE_FILTER = carFormData.workTypeName
    RESERVE_DATE = carFormData.inputDate

    var dbDatas : Seq[CarViewer] = null;
    //var stam = carDAO.selectReserve2(1,ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE)
    // dbデータ取得
    if(RESERVE_DATE!=""){
      dbDatas = carDAO.selectCarMasterSearch(placeId,ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE)
    }else{
      dbDatas = carDAO.selectCarMasterReserve(placeId)
    }
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      carListApi = carListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }

    Ok(views.html.site.itemCarReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
      ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if (super.isCmsLogged) {
      // 初期化
      init();
      val placeId = super.getCurrentPlaceId
      //検索側データ取得
      getSearchData(placeId)

      // dbデータ取得
      val dbDatas = carDAO.selectCarMasterReserve(placeId)
      var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

      // 全体から空いてるものだけ表示する。
      //carListApi = carListApi.filter(_.reserve_id == -1)

      System.out.println("carListApi:" + carListApi.length)
      Ok(views.html.site.itemCarReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
        ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
