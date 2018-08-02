package controllers.analysis

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
  * 作業車稼働状況画面
  *
  *
  */

@Singleton
class MovementCar @Inject()(config: Configuration
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
  var DETECT_MONTH = "";

  val movementCarSearchForm = Form(mapping(
    "inputDate" -> text
  )(MovementCarSearchData.apply)(MovementCarSearchData.unapply))

  /*登録用*/

  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id

  /** 　初期化 */
  def init(): Unit = {

    // 現在時刻設定
    val mSimpleDateFormat = new SimpleDateFormat("yyyy/MM", Locale.JAPAN)
    val currentTime = new Date();
    val mTime = mSimpleDateFormat.format(currentTime)
    DETECT_MONTH = mTime
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
  }

  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    System.out.println("start search:")
    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)

    // 検索情報
    val searchForm = movementCarSearchForm.bindFromRequest.get
    DETECT_MONTH = searchForm.inputDate

    val dbDatas = carDAO.selectCarMasterReserve(placeId,itemIdList)
    var carListApi = beaconService.getItemCarBeaconPosition(dbDatas,true,placeId)

    Ok(views.html.analysis.movementCar(DETECT_MONTH,carListApi))
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
    Ok(views.html.analysis.movementCar(DETECT_MONTH,carListApi))
  }

}
