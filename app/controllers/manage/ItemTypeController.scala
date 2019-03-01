package controllers.manage

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.site
import models.ItemCategoryEnum
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 仮設材種別管理アクションクラス
  *
  *
  */

// フォームの定義
case class ItemTypeUpdateForm(inputPlaceId: String,
                              inputItemTypeId: String,
                              inputItemTypeName: String,
                              inputItemTypeCategory: String,
                              inputItemTypeIconColor: String,
                              inputItemTypeTextColor: String,
                              inputItemTypeRowColor: String,
                              inputNote: String)
case class ItemTypeDeleteForm(deleteItemTypeId: String)

@Singleton
class ItemTypeController @Inject()(config: Configuration
                              , val silhouette: Silhouette[MyEnv]
                              , val messagesApi: MessagesApi
                              , itemTypeDAO: models.ItemTypeDAO
                              , carDAO: models.manage.ItemCarDAO
                              , itemOtherDAO: models.manage.ItemOtherDAO
                              , reserveMasterDAO: models.site.ReserveMasterDAO
                               ) extends BaseController with I18nSupport {

  var ITEM_TYPE_FILTER = "";
  /*enum形*/
  val ITEM_TYPE = ItemCategoryEnum().map;

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    ITEM_TYPE_FILTER = ""
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 仮設材種別情報
      val itemTypeList = itemTypeDAO.selectItemTypeInfo(placeId)
      Ok(views.html.manage.itemType(ITEM_TYPE_FILTER, ITEM_TYPE, itemTypeList, placeId))
    }else {
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      , "inputItemTypeId" -> text
      , "inputItemTypeName" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeName.empty"), {!_.isEmpty})
      , "inputItemTypeCategory" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeCategory.empty"), {_.matches("^[0-9]+$")})
      , "inputItemTypeIconColor" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeIconColor.empty"), {!_.isEmpty})
      , "inputItemTypeTextColor" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeTextColor.empty"), {!_.isEmpty})
      , "inputItemTypeRowColor" -> text.verifying(Messages("error.cms.ItemTypeManage.update.inputItemTypeRowColor.empty"), {!_.isEmpty})
      , "inputNote" -> text
    )(ItemTypeUpdateForm.apply)(ItemTypeUpdateForm.unapply))

    // 権限レベルを取得
    val reqIdentity = request.identity
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)

      // リダイレクトで画面遷移
      Redirect(routes.ItemTypeController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      var errMsg = Seq[String]()
      val f = form.get

      if(checkRGB(f.inputItemTypeIconColor)){
        if(checkRGB(f.inputItemTypeTextColor)){
          if(checkRGB(f.inputItemTypeRowColor)){
            if(f.inputItemTypeId.isEmpty){
              // 新規登録の場合 --------------------------
              // DB処理
              itemTypeDAO.insert(f.inputItemTypeName, f.inputItemTypeCategory.toInt, f.inputItemTypeIconColor, f.inputItemTypeTextColor, f.inputItemTypeRowColor, f.inputNote, f.inputPlaceId.toInt)

              Redirect(routes.ItemTypeController.index)
                .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.update"))

            }else{
              // 更新の場合 --------------------------
              // 予約情報テーブルに作業車・立馬の仮設材種別使用チェック
              val reserveList = reserveMasterDAO.selectReserveItemTypeCheck(super.getCurrentPlaceId, f.inputItemTypeId.toInt)
              if (reserveList.length > 0) {
                var chkFlg: Int = 0
                // 現在時刻設定
                val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
                val currentTime = new Date();
                val mTime = mSimpleDateFormat.format(currentTime)
                val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
                for (itemCarReserve <- reserveList) {
                  if (itemCarReserve.reserveEndDate.toInt < mTime.toInt) {
                    // 予約期間が前日の場合、または期間が終日の場合
                    if (chkFlg != 1) {
                      chkFlg = 2
                    }

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
                      } else {
                        // 予約済み（使用中）
                        chkFlg = 1
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
                      } else {
                        // 予約済み（使用中）
                        chkFlg = 1
                      }
                    } else {
                      // 未来で予約済み
                      chkFlg = 1
                    }
                  }
                }
                if (chkFlg == 1) {
                  errMsg :+= Messages("error.cms.ItemTypeManage.update.ItemTypeReserve", f.inputItemTypeId)
                } else if (chkFlg == 2) {
                  if(reqIdentity.level < 3) {
                    // 権限がレベル３以下のみ予約情報が有る場合エラーにする
                    errMsg :+= Messages("error.cms.ItemTypeManage.update.ItemTypeReserve.use.noChange", f.inputItemTypeId)
                  }
                }
              }
              if(errMsg.nonEmpty){
                // エラーで遷移
                Redirect(routes.ItemTypeController.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
              }else {
                // DB処理
                itemTypeDAO.updateById(f.inputItemTypeId.toInt, f.inputItemTypeName, f.inputItemTypeCategory.toInt, f.inputItemTypeIconColor, f.inputItemTypeTextColor, f.inputItemTypeRowColor, f.inputNote)

                Redirect(routes.ItemTypeController.index).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.update"))
              }
            }
          }else{
            Redirect(routes.ItemTypeController.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.ItemTypeManage.RowColor"))
          }
        }else {
          Redirect(routes.ItemTypeController.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.ItemTypeManage.TextColor"))
        }

      }else{
        Redirect(routes.ItemTypeController.index()).flashing(ERROR_MSG_KEY -> Messages("error.cms.ItemTypeManage.IconColor"))
      }
    }
  }

  /** RGBチェック「rgb(255,255,255)形式」 */
  def checkRGB(Inpar: String): Boolean = {
    // RGB 範囲チェック
    var resFlg: Boolean = true
    var rgbWk = Inpar
    var rgb = rgbWk.substring(4, rgbWk.length())
    rgb = rgb.replace(")", "")
    val listRgb = rgb.split(",")
    if(listRgb.length == 3) {
      for (rgbInt <- listRgb) {
        if (rgbInt.trim().toInt < 0 || rgbInt.trim().toInt > 255) {
          resFlg = false
        }
      }
    }else{
      resFlg = false
    }
    return resFlg
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // 権限レベルを取得
    val reqIdentity = request.identity
    // フォームの準備
    val inputForm = Form(mapping(
      "deleteItemTypeId" -> text.verifying(Messages("error.cms.ItemTypeManage.delete.empty"), {!_.isEmpty})
    )(ItemTypeDeleteForm.apply)(ItemTypeDeleteForm.unapply))
    val placeId = super.getCurrentPlaceId
    // メッセージ格納用
    var errMsg = Seq[String]()
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemTypeController.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get
      // 作業車・立馬マスタ　仮設材種別使用チェック
      val itemCarList = carDAO.selectItemTypeCheck(placeId.toInt, f.deleteItemTypeId.toInt)
      if(itemCarList.length > 0){
        errMsg :+= Messages("error.cms.ItemTypeManage.delete.ItemTypeCar", f.deleteItemTypeId)
      }
      // その他仮設材マスタ　仮設材種別使用チェック
      val itemOtherList = itemOtherDAO.selectItemTypeCheck(placeId.toInt, f.deleteItemTypeId.toInt)
      if(itemOtherList.length > 0){
        errMsg :+= Messages("error.cms.ItemTypeManage.delete.ItemTypeOther", f.deleteItemTypeId)
      }

      // 予約情報テーブルに作業車・立馬の仮設材種別使用チェック
      val reserveList = reserveMasterDAO.selectReserveItemTypeCheck(super.getCurrentPlaceId, f.deleteItemTypeId.toInt)
      if (reserveList.length > 0) {
        var chkFlg: Int = 0
        // 現在時刻設定
        val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
        val currentTime = new Date();
        val mTime = mSimpleDateFormat.format(currentTime)
        val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
        for (itemCarReserve <- reserveList) {
          if (itemCarReserve.reserveEndDate.toInt < mTime.toInt) {
            // 予約期間が前日の場合、または期間が終日の場合
            if (chkFlg != 1) {
              chkFlg = 2
            }

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
              } else {
                // 予約済み（使用中）
                chkFlg = 1
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
              } else {
                // 予約済み（使用中）
                chkFlg = 1
              }
            } else {
              // 未来で予約済み
              chkFlg = 1
            }
          }
        }
        if (chkFlg == 1) {
          errMsg :+= Messages("error.cms.ItemTypeManage.delete.ItemTypeReserve", f.deleteItemTypeId)
        } else if (chkFlg == 2) {
          if(reqIdentity.level < 3) {
            // 権限がレベル３以下のみ予約情報が有る場合エラーにする
            errMsg :+= Messages("error.cms.ItemTypeManage.delete.ItemTypeReserve.use.noChange", f.deleteItemTypeId)
          }
        }
      }
      if(errMsg.nonEmpty){
        // エラーで遷移
        Redirect(routes.ItemTypeController.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      }else{
        // DB処理
        itemTypeDAO.deleteById(f.deleteItemTypeId.toInt)

        // リダイレクト
        Redirect(routes.ItemTypeController.index).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemTypeManage.delete"))
      }
    }
  }

}
