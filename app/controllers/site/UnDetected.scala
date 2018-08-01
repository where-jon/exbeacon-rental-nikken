package controllers.site

import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService}
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import utils.silhouette.MyEnv

/**
  * 未検出の仮設材画面
  *
  *
  */


@Singleton
class UnDetected @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , carDAO: models.itemCarDAO
                           , itemLogDao: models.ItemLogDAO
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
  var FLOOR_NAME_FILTER = "";
  var DETECT_DATE = "";

  var itemTypeList: Seq[ItemType] = null; // 仮設材種別
  var floorNameList: Seq[Floor] = null; // フロア

  val unDetectedSearchForm = Form(mapping(
    "itemTypeId" -> number,
    "floorName" -> text,
    "inputDate" -> text
  )(UnDetectedSearchData.apply)(UnDetectedSearchData.unapply))

  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    FLOOR_NAME_FILTER = ""

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    DETECT_DATE = mTime

  }

  /** 　検索側データ取得 */
  def getSearchData(_placeId: Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectItemTypeInfo(_placeId);
    /*フロア取得*/
    floorNameList = floorDAO.selectFloor(_placeId);
  }

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 検索情報
    val searchForm = unDetectedSearchForm.bindFromRequest.get
    ITEM_TYPE_FILTER = searchForm.itemTypeId
    FLOOR_NAME_FILTER = searchForm.floorName
    DETECT_DATE = searchForm.inputDate

    var dbDatasList = itemLogDao.selectUnDetectedData(placeId,DETECT_DATE)

    if (FLOOR_NAME_FILTER != "") {
      dbDatasList = dbDatasList.filter(_.finish_floor_name == FLOOR_NAME_FILTER)
    }

    if (ITEM_TYPE_FILTER != 0) {
      dbDatasList = dbDatasList.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }


    Ok(views.html.site.unDetected(ITEM_TYPE_FILTER, FLOOR_NAME_FILTER,DETECT_DATE
      , dbDatasList, itemTypeList, floorNameList))
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 初期化
    init();
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // dbデータ取得
    val dbDatasList = itemLogDao.selectUnDetectedData(placeId,DETECT_DATE)

    Ok(views.html.site.unDetected(ITEM_TYPE_FILTER, FLOOR_NAME_FILTER, DETECT_DATE
      , dbDatasList, itemTypeList, floorNameList))
  }

}
