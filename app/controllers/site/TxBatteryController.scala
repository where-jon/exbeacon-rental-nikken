package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService, errors}
import models.{ItemType, PowerEnum}
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws._
import utils.silhouette.MyEnv

case class TxSearchForm(powerValue: String,itemTypeId:String)
/**
  * BeaconTxタグ管理アクションクラス
  *
  *
  */
@Singleton
class TxBatteryController @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , ws: WSClient
  , itemTypeDAO: models.ItemTypeDAO
  , beaconService: BeaconService
  , beaconDAO: models.beaconDAO
  ) extends BaseController with I18nSupport {

  /*検索用*/
  var ITEM_TYPE_FILTER = 0
  var POWER_FILTER = 0

  var itemTypeList :Seq[ItemType] = null // 仮設材種別
  var itemIdList :Seq[Int] = null // 仮設材種別id


  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
    POWER_FILTER = 0
  }


  /** 　検索側データ取得 */
  def getSearchData(_placeId:Integer): Unit = {
    /*仮設材種別取得*/
    itemTypeList = itemTypeDAO.selectTotalItemInfo(_placeId);
    /*仮設材種別id取得*/
    itemIdList = itemTypeList.map{item => item.item_type_id}
    if(itemIdList.isEmpty){
      itemIdList = Seq(-1)
    }
  }


  /** 　検索ロジック */
  def search = SecuredAction { implicit request =>
    val searchForm = Form(mapping(
      "powerValue" -> text
      ,"itemTypeId" -> text
    )(TxSearchForm.apply)(TxSearchForm.unapply))
    val searchFormData = searchForm.bindFromRequest.get

    ITEM_TYPE_FILTER = searchFormData.itemTypeId.toInt
    POWER_FILTER = searchFormData.powerValue.toInt

    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)
    // ①仮設材すべてを取得
    val dbDatas = beaconDAO.selectBeaconViewer(placeId)
    // ②仮設材種別作業車の鍵をものとして取得
    val dbDatasKeyData = beaconDAO.selectCarKeyViewer(placeId)
    // ①、②を結合
    val totalDbDatas = dbDatas union  dbDatasKeyData
    var beaconListApi = beaconService.getTxData(totalDbDatas,true,placeId)

    if (ITEM_TYPE_FILTER != 0) {
      beaconListApi = beaconListApi.filter(_.item_type_id == ITEM_TYPE_FILTER)
    }

    val POWER_ENUM = PowerEnum().map;
    if (POWER_FILTER != 0) {
      if(POWER_FILTER == POWER_ENUM.toList(3)._1) {
        // 未検出
        beaconListApi = beaconListApi.filter(_.power_level == POWER_FILTER)
      }else if(POWER_FILTER <= POWER_ENUM.toList(2)._1){
        // 交換
        beaconListApi = beaconListApi.filter(_.power_level <= POWER_FILTER).filter(_.power_level >= 0)
      }else if ( POWER_FILTER <= POWER_ENUM.toList(1)._1){
        // 注意
        beaconListApi = beaconListApi.filter(_.power_level <= POWER_FILTER).filter(_.power_level > POWER_ENUM.toList(2)._1)
      }else{
        // 良好
        beaconListApi = beaconListApi.filter(_.power_level >= POWER_FILTER)
      }
    }
    Ok(views.html.site.txBattery(POWER_ENUM,POWER_FILTER,ITEM_TYPE_FILTER,itemTypeList,beaconListApi))
  }

  /**
    * 初期表示
    * @return
    */
  def index = SecuredAction{ implicit request =>

    val placeId = super.getCurrentPlaceId
    if(beaconService.getCloudUrl(placeId)){
      //検索側データ取得
      getSearchData(placeId)
      // ①仮設材すべてを取得
      val dbDatas = beaconDAO.selectBeaconViewer(placeId)
      // ②仮設材種別作業車の鍵をものとして取得
      val dbDatasKeyData = beaconDAO.selectCarKeyViewer(placeId)
      // ①、②を結合
      val totalDbDatas = dbDatas union  dbDatasKeyData
      val beaconListApi = beaconService.getTxData(totalDbDatas,true,placeId)

      if(beaconListApi!=null){
        val POWER_ENUM = PowerEnum().map;
        Ok(views.html.site.txBattery(POWER_ENUM,POWER_FILTER,ITEM_TYPE_FILTER,itemTypeList,beaconListApi))
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
