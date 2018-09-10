package controllers.site

import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
import models.{ItemCarReserveData, _}
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

  /** 　予約ロジック */
  def reserve = SecuredAction { implicit request =>
    // dbデータ取得
    val placeId = super.getCurrentPlaceId
    getSearchData(placeId)

    /*転送form*/
    val itemCarForm = Form(mapping(
      "itemTypeId" -> number,
      "workTypeName" -> text.verifying(Messages("error.site.itemReserve.form.workType"), { workTypeName => !workTypeName.isEmpty() }),
      "inputDate" -> text.verifying(Messages("error.site.itemReserve.form.reserveDate"), { inputDate => !inputDate.isEmpty() }),
      "companyName" -> text.verifying(Messages("error.site.itemReserve.form.company"), { companyName => !companyName.isEmpty() }),
      "floorName" -> text.verifying(Messages("error.site.itemReserve.form.floor"), { floorName => !floorName.isEmpty() }),
      "itemId" -> list(number.verifying(Messages("error.site.itemReserve.form.id"), { itemId => itemId != null })),
      "itemTypeIdList" -> list(number.verifying(Messages("error.site.itemReserve.form.type"), { itemTypeIdList => itemTypeIdList != null })),
      "checkVal" -> list(number.verifying(Messages("error.site.itemReserve.form.select"), { checkVal => checkVal != null }))
    )(ItemCarReserveData.apply)(ItemCarReserveData.unapply))

    val form = itemCarForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemCarReserve.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      val ItemCarReserveData = form.get
      if(ItemCarReserveData.checkVal.zipWithIndex.length > 0){
        val vCurrentTimeCheck =
          beaconService.currentTimeReserveCheck(ItemCarReserveData.inputDate,ItemCarReserveData.workTypeName)
        if(vCurrentTimeCheck == "OK"){  // 現在時刻から予約可能かを判定
          var setData = List[ReserveItem]()
          val vCompanyId = companyNameList.filter(_.companyName == ItemCarReserveData.companyName).last.companyId
          val vFloorId = floorNameList.filter(_.floor_name == ItemCarReserveData.floorName).last.floor_Id
          val vWorkTypeId = workTypeList.filter(_.work_type_name == ItemCarReserveData.workTypeName).last.work_type_id
          val vReserveDate = ItemCarReserveData.inputDate
          var idListData = List[Int]()
          var idTypeListData = List[Int]()
          ItemCarReserveData.itemId.zipWithIndex.map { case (itemId, i) =>
            ItemCarReserveData.checkVal.zipWithIndex.map { case (check, j) =>
              if(i == check){
                val vItemTypeId = ItemCarReserveData.itemTypeIdList(i)
                idListData = idListData :+itemId
                idTypeListData = idTypeListData :+vItemTypeId
                setData = setData :+ ReserveItem(vItemTypeId,itemId,vFloorId,placeId,vCompanyId,vReserveDate,vReserveDate,true,vWorkTypeId)
              }
            }
          }
          var errMsg = Seq[String]()

          // 検索ロジック追加あるかどうかを判断する
          val vAlerdyReserveData = reserveMasterDAO.selectCarReserve(placeId,idListData,idTypeListData,vWorkTypeId,vReserveDate,vReserveDate)
          System.out.println("vCount :" + vAlerdyReserveData)
          if(vAlerdyReserveData.isEmpty){ // 予約されたものがない
            val result = carDAO.reserveItemCar(setData)
            if (result == "success") {
              Redirect(routes.ItemCarReserve.index())
                .flashing(SUCCESS_MSG_KEY -> Messages("success.site.carReserve.update"))
            }else {
              Redirect(routes.ItemCarReserve.index())
                .flashing(ERROR_MSG_KEY -> Messages("error.site.carReserve.update"))
            }
          }else { // 予約されたものがある
            errMsg :+= Messages("error.site.itemCarReserve.reserve")
            errMsg :+= Messages("error.site.itemCarReserve.id" ,vAlerdyReserveData.last.txId)
            errMsg :+= Messages("error.site.itemCarReserve.workType" ,vAlerdyReserveData.last.workTypeName)
            errMsg :+= Messages("error.site.itemCarReserve.reserveDate"  ,vAlerdyReserveData.last.reserveStartDate)
            errMsg :+= Messages("error.site.itemCarReserve.already" )

            Redirect(routes.ItemCarReserve.index())
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          }
        }else{  // 現在時刻から予約可能かを判定でエラーの場合
          if(vCurrentTimeCheck == "当日"){
            val currentTime = new Date();
            val mSimpleDateFormatHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
            val mCurrentTime = mSimpleDateFormatHour.format(currentTime)
            var errMsg = Seq[String]()
            errMsg :+= Messages("error.site.reserve.overtime", mCurrentTime,ItemCarReserveData.workTypeName)
            errMsg :+= Messages("error.site.reserve.overtime.define")
            Redirect(routes.ItemCarReserve.index())
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          } else {
            Redirect(routes.ItemCarReserve.index())
              .flashing(ERROR_MSG_KEY -> Messages(vCurrentTimeCheck))
          }
        }
      }else{
        Redirect(routes.ItemCarReserve.index())
          .flashing(ERROR_MSG_KEY -> Messages("error.site.carReserve.noselect"))
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


    Ok(views.html.site.itemCarReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
      ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
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
      val dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
      val carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)
      if(carListApi!=null){
        Ok(views.html.site.itemCarReserve(ITEM_TYPE_FILTER,WORK_TYPE_FILTER,RESERVE_DATE
          ,carListApi,itemTypeList,companyNameList,floorNameList,workTypeList,WORK_TYPE))
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
