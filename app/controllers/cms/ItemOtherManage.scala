package controllers.cms

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.site
import models.{ItemCategoryEnum, ItemType}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * その他仮設材管理アクションクラス
  *
  *
  */
// フォーム定義
case class ItemDeleteForm(
    deleteItemOtherId: String
)
case class ItemUpdateForm(
    inputPlaceId: String
  , inputItemOtherId: String
  , inputItemOtherBtxId: String
  , inputItemOtherNo: String
  , inputItemOtherName: String
  , inputItemNote: String
  , inputItemTypeName: String
  , inputItemTypeId: String
)

@Singleton
class ItemOtherManage @Inject()(
  config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , itemOtherDAO: models.itemOtherDAO
  , itemDAO: models.itemDAO
  , itemTypeDAO:models.ItemTypeDAO
  , carDAO: models.itemCarDAO
) extends BaseController with I18nSupport {

  /*enum形*/
  var ITEM_TYPE_FILTER = 0;
  val ITEM_TYPE = ItemCategoryEnum().map;

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 仮設材情報
      val itemTypeList = itemTypeDAO.selectItemOtherInfoOrder(placeId)

      // その他仮設材情報
      val itemOtherList = itemOtherDAO.selectOtherMasterInfo(placeId)
      Ok(views.html.cms.itemOtherManage(ITEM_TYPE_FILTER, ITEM_TYPE, itemOtherList, itemTypeList, placeId))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputItemOtherId" -> text
      , "inputItemOtherBtxId" -> text.verifying(Messages("error.cms.ItemOtherManage.update.inputItemOtherBtxId.empty"), {_.matches("^[0-9]+$")})
      , "inputItemOtherNo" -> text.verifying(Messages("error.cms.ItemOtherManage.update.inputItemOtherNo.empty"), {!_.isEmpty})
      , "inputItemOtherName" -> text.verifying(Messages("error.cms.ItemOtherManage.update.inputItemOtherName.empty"), {!_.isEmpty})
      , "inputItemNote" -> text
      , "inputItemTypeName" -> text.verifying(Messages("error.cms.ItemOtherManage.update.inputItemTypeName.empty"), {!_.isEmpty})
      , "inputItemTypeId" -> text
    )(ItemUpdateForm.apply)(ItemUpdateForm.unapply))
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemOtherManage.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      var errMsg = Seq[String]()
      val f = form.get
      // 種別存在チェック
      val itemTypeList = itemTypeDAO.selectItemTypeCheck(f.inputItemTypeName, f.inputPlaceId.toInt)
      if(itemTypeList.isEmpty){
        errMsg :+= Messages("error.cms.CarManage.update.NotTypeName", f.inputItemOtherNo)
      }
      if(f.inputItemOtherId.isEmpty){
        // 新規登録
        // TxビーコンIDが存在しないか
        val itemOtherBtxList = itemOtherDAO.selectItemOtherBtxListBtxCheck(super.getCurrentPlaceId, f.inputItemOtherBtxId.toInt)
        if(itemOtherBtxList.length > 0){
          errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemBtxId.use", f.inputItemOtherBtxId)
        }
        // 同じIDが作業車・立馬に存在してはダメ
        val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, f.inputItemOtherBtxId.toInt)
        if (chkCarTagIdInf.length > 0) {
          errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemBtxId.useCar", f.inputItemOtherBtxId)
        }
        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.ItemOtherManage.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        } else {
          var sss: Int = 0
          sss = sss + 1
          // 登録の実行
          itemOtherDAO.insert(
                            f.inputItemOtherNo,
                            f.inputItemOtherName,
                            f.inputItemOtherBtxId.toInt,
                            f.inputItemNote,
                            f.inputItemTypeId.toInt,
                            super.getCurrentPlaceId)
          Redirect(routes.ItemOtherManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemOtherManage.update"))
        }
      } else {
        // 更新の場合 --------------------------
        // その他仮設材重複チェック用
        var dbItemNoList = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, f.inputItemOtherNo)
        dbItemNoList = dbItemNoList.filter(_.itemOtherId != f.inputItemOtherId.toInt)
                                   .filter(_.itemOtherNo == f.inputItemOtherNo)
        // 予約情報テーブルに作業車・立馬IDが存在していないか
        val itemOtherReserveList = itemOtherDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.inputItemOtherId.toInt)
        if(itemOtherReserveList.length > 0){
          var chkFlg : Int = 0
          // 現在時刻設定
          val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
          val currentTime = new Date();
          val mTime = mSimpleDateFormat.format(currentTime)
          val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
          for(itemCarReserve <- itemOtherReserveList) {
            if (itemCarReserve.reserveEndDate.toInt < mTime.toInt) {
              // 予約期間が前日の場合、または期間が終日の場合
              chkFlg = 2

            } else {
              // 当日以降
              if (itemCarReserve.workTypeId == 1) {
                // 午前予約の場合
                if (itemCarReserve.reserveEndDate.toInt == mTime.toInt) {
                  if (mHour < 13) {
                    // 予約済み（使用中）
                    chkFlg = 1
                  } else {
                    // 現在時刻が13時を過ぎていた場合
                    if (chkFlg != 1) {
                      chkFlg = 2
                    }
                  }
                }
              } else if (itemCarReserve.workTypeId == 2) {
                // 午後予約の場合
                if (itemCarReserve.reserveEndDate.toInt == mTime.toInt) {
                  if (mHour < 17) {
                    // 予約済み（使用中）
                    chkFlg = 1
                  } else {
                    // 現在時刻が17時を過ぎていた場合
                    if (chkFlg != 1) {
                      chkFlg = 2
                    }
                  }
                }
              } else {
                // 未来で予約済み
                chkFlg = 1
              }
            }
          }
          if(chkFlg == 1){
            errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemOtherIdReserve.use.noChange", f.inputItemOtherId);
          }else if(chkFlg == 2){
            errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemOtherIdReserve.use.exceed", f.inputItemOtherId);
          }
        }
        // 同じIDが作業車・立馬に存在してはダメ
        val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, f.inputItemOtherBtxId.toInt)
        if (chkCarTagIdInf.length > 0) {
          errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemBtxId.useCar", f.inputItemOtherBtxId)
        }
        // 変更前タグ情報
        val preItemInfo = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, "", Option(f.inputItemOtherId.toInt)).last
        // タグチェック
        val checkList = Seq[(Int,Int)](
          (preItemInfo.itemOtherBtxId, f.inputItemOtherBtxId.toInt)
        )
        checkList.zipWithIndex.foreach { case (btxId, i) =>
          if (btxId._1 == btxId._2) {
            // 変更前と同じの場合何もしない
          } else {
            // 登録が重複する場合
            // その他仮設材TxビーコンID重複チェック
            var btxList = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, "", None, Option(f.inputItemOtherBtxId.toInt))
            if(btxList.length > 0){
              errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemNo.duplicate", f.inputItemOtherBtxId)
            }
          }
        }
        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.ItemOtherManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // 更新の実行
          itemOtherDAO.update(
                              f.inputItemOtherId.toInt,
                              f.inputItemOtherNo,
                              f.inputItemOtherName,
                              f.inputItemOtherBtxId.toInt,
                              f.inputItemNote,
                              f.inputItemTypeId.toInt,
                              super.getCurrentPlaceId)
          // リダイレクト
          Redirect(routes.ItemOtherManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemOtherManage.update"))
        }
      }
    }
  }

  /** その他仮設材削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    var errMsg = Seq[String]()
    val inputForm = Form(mapping(
      "deleteItemOtherId" -> text.verifying(Messages("error.cms.ItemOtherManage.delete.empty"), {
        !_.isEmpty
      })
    )(ItemDeleteForm.apply)(ItemDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemOtherManage.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get

      // 予約情報テーブルに作業車・立馬IDが存在していないか
      val itemOtherReserveList = itemOtherDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.deleteItemOtherId.toInt)
      if(itemOtherReserveList.length > 0){
        var chkFlg : Int = 0
        // 現在時刻設定
        val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
        val currentTime = new Date();
        val mTime = mSimpleDateFormat.format(currentTime)
        val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
        for(itemCarReserve <- itemOtherReserveList) {
          if (itemCarReserve.reserveEndDate.toInt < mTime.toInt) {
            // 予約期間が前日の場合、または期間が終日の場合
            chkFlg = 2

          } else {
            // 当日以降
            if (itemCarReserve.workTypeId == 1) {
              // 午前予約の場合
              if (itemCarReserve.reserveEndDate.toInt == mTime.toInt) {
                if (mHour < 13) {
                  // 予約済み（使用中）
                  chkFlg = 1
                } else {
                  // 現在時刻が13時を過ぎていた場合
                  if (chkFlg != 1) {
                    chkFlg = 2
                  }
                }
              }
            } else if (itemCarReserve.workTypeId == 2) {
              // 午後予約の場合
              if (itemCarReserve.reserveEndDate.toInt == mTime.toInt) {
                if (mHour < 17) {
                  // 予約済み（使用中）
                  chkFlg = 1
                } else {
                  // 現在時刻が17時を過ぎていた場合
                  if (chkFlg != 1) {
                    chkFlg = 2
                  }
                }
              }
            } else {
              // 未来で予約済み
              chkFlg = 1
            }
          }
        }
        if(chkFlg == 1){
          errMsg :+= Messages("error.cms.ItemOtherManage.delete.inputItemOtherIdReserve.use.noChange", f.deleteItemOtherId);
        }else if(chkFlg == 2){
          errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemOtherIdReserve.use.exceed", f.deleteItemOtherId);
        }
      }

      if(errMsg.nonEmpty){
        // エラーで遷移
        Redirect(routes.ItemOtherManage.index)
          .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      }else{
        // 削除
        itemOtherDAO.delete(f.deleteItemOtherId.toInt, super.getCurrentPlaceId)
        // リダイレクト
        Redirect(routes.ItemOtherManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemOtherManage.delete"))
      }
    }
  }
}
