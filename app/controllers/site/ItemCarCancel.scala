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
  var companyNameList :Seq[Company] = null; // 業者
  var floorNameList :Seq[Floor] = null; // フロア
  var workTypeList :Seq[WorkType] = null; // 作業期間種別

  /*enum形*/
  val WORK_TYPE = WorkTypeEnum().map;

  /*転送form*/
  val itemCarForm = Form(mapping(
    "itemTypeId" -> number.verifying("仮設材未設定", { itemTypeId => itemTypeId != null }),
    "workTypeName" -> text.verifying("作業期間未設定", { workTypeName => !workTypeName.isEmpty() }),
    "inputDate" -> text.verifying("予約日未設定", { inputDate => !inputDate.isEmpty() }),
    "companyName" -> text.verifying("予約会社未設定", { companyName => !companyName.isEmpty() }),
    "floorName" -> text.verifying("予約フロア未設定", { floorName => !floorName.isEmpty() }),
    "itemId" -> list(number.verifying("仮設材IDが異常", { itemId => itemId != null })),
    "checkVal" -> list(number.verifying("選択", { itemId => itemId != null }))
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
    /*作業期間種別取得*/
    workTypeList = workTypeDAO.selectWorkInfo(_placeId);

    /*業者取得*/
    companyNameList = companyDAO.selectCompany(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);
  }
  /** 　検索側データ取得 */
  def setReserveData(_placeId:Integer): Unit = {

  }


  /** 　予約ロジック */
  def reserve = SecuredAction { implicit request =>
    System.out.println("---reserve:----" )
    // dbデータ取得
    val placeId = super.getCurrentPlaceId
    getSearchData(placeId)
    val dbDatas = carDAO.selectCarMasterReserve(placeId)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
    //val carFormData = itemCarForm.bindFromRequest.get
    itemCarForm.bindFromRequest.fold(
      formWithErrors =>
      Redirect(routes.ItemCarReserve.index())
          .flashing(ERROR_MSG_KEY -> Messages(formWithErrors.errors.map(_.message +"<br>").mkString("\n"))),

      ItemCarReserveData => {
        if(ItemCarReserveData.checkVal.zipWithIndex.length > 0){
            var setData = List[ReserveItem]()

            var vCompanyId = companyNameList.filter(_.companyName == ItemCarReserveData.companyName).last.companyId
            var vFloorId = floorNameList.filter(_.floor_name == ItemCarReserveData.floorName).last.floor_Id
            var vWorkTypeId = workTypeList.filter(_.work_type_name == ItemCarReserveData.workTypeName).last.work_type_id
            var vItemTypeId = ItemCarReserveData.itemTypeId
            var vReserveDate = ItemCarReserveData.inputDate

          ItemCarReserveData.itemId.zipWithIndex.map { case (itemId, i) =>
            ItemCarReserveData.checkVal.zipWithIndex.map { case (check, j) =>
                if(i == check){
                  setData = setData :+ ReserveItem(vItemTypeId,itemId,vFloorId,placeId,vCompanyId,vReserveDate,vReserveDate,true,vWorkTypeId)
                }
              }
            }
            val result = carDAO.reserveItemCar(setData)
            if (result == "success") {
              Redirect(routes.ItemCarReserve.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.carReserve.update"))
            }else {
              Redirect(routes.ItemCarReserve.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.carReserve.update"))
            }
        }else{
          Redirect(routes.ItemCarReserve.index())
            .flashing(ERROR_MSG_KEY -> Messages("予約対象未選択"))
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
    WORK_TYPE_FILTER = carFormSearchData.workTypeName
    RESERVE_DATE = carFormSearchData.inputDate

    var dbDatas : Seq[CarViewer] = null;
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

    Ok(views.html.site.itemCarCancel(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
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
      Ok(views.html.site.itemCarCancel(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
        ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
