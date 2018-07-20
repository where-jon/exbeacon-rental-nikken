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

/*作業車・立馬予約用クラス*/
case class ReserveItem(
  item_type_id :Int,
  item_id :Int,
  floor_id :Int,
  place_id :Int,
  company_id :Int,
  reserve_start_date:String,
  reserve_end_date:String,
  active_flg:Boolean,
  work_type_id :Int
)

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



  /*作業車・立馬予約用formクラス*/
  case class ItemCarReserveData(
     itemTypeId: Int,
     workTypeName: String,
     inputDate: String,
     companyName: String,
     floorName: String,

     itemId: List[Int],
     checkVal: List[Int]

   )


  /*転送form*/
  val itemCarForm = Form(mapping(
    "itemTypeId" -> number,
    "workTypeName" -> text,
    "inputDate" -> text,
    "companyName" -> text,
    "floorName" -> text,
    "itemId" -> list(number),
    "checkVal" -> list(number)
  )(ItemCarReserveData.apply)(ItemCarReserveData.unapply))

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
    val carFormData = itemCarForm.bindFromRequest.get
    itemCarForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.site.itemCarReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
        ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE)),
      ItemCarReserveData => {
        val itemCarData = itemCarForm.bindFromRequest.get
        var setData = List[ReserveItem]()

        var vCompanyId = companyNameList.filter(_.companyName == carFormData.companyName).last.companyId
        var vFloorId = floorNameList.filter(_.floor_name == carFormData.floorName).last.floor_Id
        var vWorkTypeId = workTypeList.filter(_.work_type_name == carFormData.workTypeName).last.work_type_id
        var vItemTypeId = itemCarData.itemTypeId
        var vReserveDate = itemCarData.inputDate

        itemCarData.itemId.zipWithIndex.map { case (itemId, i) =>
          itemCarData.checkVal.zipWithIndex.map { case (check, j) =>
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
    val carFormData = itemCarForm.bindFromRequest.get
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
