package controllers.site

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
import javax.inject.{Inject, Singleton}
import models._
import models.manage._
import models.system.Floor
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * その他仮設材一覧クラス
  *
  *
  */


@Singleton
class ItemOtherListController @Inject()(config: Configuration
, val silhouette: Silhouette[MyEnv]
, val messagesApi: MessagesApi
, otherDAO: models.manage.ItemOtherDAO
, companyDAO: models.manage.CompanyDAO
, beaconService: BeaconService
, floorDAO: models.system.floorDAO
, btxDAO: models.btxDAO
, itemTypeDAO: models.manage.ItemTypeDAO
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
  val WORK_TYPE_ID = WorkTypeIdEnum().map;

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
    itemTypeList = itemTypeDAO.selectItemOtherInfo(_placeId);
    /*仮設材種別id取得*/
    itemIdList = itemTypeList.map{item => item.item_type_id}
    if(itemIdList.isEmpty){
      itemIdList = Seq(-1)
    }
    /*業者取得*/
    companyNameList = companyDAO.selectCompany(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);
    /*作業期間種別取得*/
    workTypeList = workTypeDAO.selectWorkInfo(_placeId);
  }

  /** workType重複を除く処理 */
  def getItemListDuplicateWorkType(dbData:Seq[OtherViewer]): Seq[OtherViewer] = {
    var preItemId = -1
    dbData.zipWithIndex.foreach { case (otherList, index) =>
      if(preItemId == otherList.item_other_id) {
        dbData(index-1).work_type_name = WORK_TYPE.get(2).last  // 終日の場合
        dbData(index-1).work_type_id = WORK_TYPE_ID.get("終日").last  // 終日の値
        otherList.work_type_name = "重複"
        dbData.drop(index)
      }
      preItemId = otherList.item_other_id
    }
    return dbData.filter(_.work_type_name != "重複")
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
    val dbDatas = this.getItemListDuplicateWorkType(otherDAO.selectOtherMasterSql(placeId,itemIdList))
    var otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)
    if (ITEM_TYPE_FILTER != 0) {
      otherListApi = otherListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }
    if (FLOOR_NAME_FILTER != "") {
      otherListApi = otherListApi.filter(_.cur_pos_name== FLOOR_NAME_FILTER)
    }
    if (COMPANY_NAME_FILTER != "") {
      otherListApi = otherListApi.filter(_.company_name == COMPANY_NAME_FILTER)
    }

    Ok(views.html.site.itemOtherList(ITEM_TYPE_FILTER,COMPANY_NAME_FILTER,FLOOR_NAME_FILTER,WORK_TYPE_FILTER
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
      val dbDatas = this.getItemListDuplicateWorkType(otherDAO.selectOtherMasterSql(placeId,itemIdList))
      val otherListApi = beaconService.getItemOtherBeaconPosition(dbDatas,true,placeId)
      if(otherListApi!=null){
        Ok(views.html.site.itemOtherList(ITEM_TYPE_FILTER, COMPANY_NAME_FILTER,FLOOR_NAME_FILTER,WORK_TYPE_FILTER
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
