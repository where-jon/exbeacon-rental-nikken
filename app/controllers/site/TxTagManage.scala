package controllers.site

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, BeaconService}
import models.{PowerEnum, ItemType}
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws._
import utils.silhouette.MyEnv


/**
  * BeaconTxタグ管理アクションクラス
  *
  *
  */
@Singleton
class TxTagManage @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , ws: WSClient
  , itemTypeDAO: models.ItemTypeDAO
  , beaconService: BeaconService
  , beaconDAO: models.beaconDAO
  ) extends BaseController with I18nSupport {

  /*検索用*/
  var ITEM_TYPE_FILTER = 0;

  var itemTypeList :Seq[ItemType] = null; // 仮設材種別
  var itemIdList :Seq[Int] = null; // 仮設材種別id




  /** 　初期化 */
  def init() {
    ITEM_TYPE_FILTER = 0
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
  /**
    * 初期表示
    * @return
    */
  def index = SecuredAction{ implicit request =>

    val placeId = super.getCurrentPlaceId
    //検索側データ取得
    getSearchData(placeId)
    // ①仮設材すべてを取得
    val dbDatas = beaconDAO.selectBeaconViewer(placeId)
    val beaconListApi = beaconService.getBeaconPosition(dbDatas,true,placeId)

    val POWER_ENUM = PowerEnum().map;
    Ok(views.html.site.txTagManage(POWER_ENUM,ITEM_TYPE_FILTER,itemTypeList,beaconListApi))
  }
}
