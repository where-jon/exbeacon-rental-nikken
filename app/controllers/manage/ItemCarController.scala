package controllers.manage

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import javax.inject.{Inject, Singleton}
import models.ItemTypeOrder
import models.manage.{CarDeleteForm, CarUpdateForm}
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 作業車・立馬管理アクションクラス
  *
  *
  */
@Singleton
class ItemCarController @Inject()(
  config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , carDAO: models.manage.ItemCarDAO
  , exbDAO: models.system.ExbDAO
  , itemTypeDAO: models.ItemTypeDAO
  , itemOtherDAO: models.manage.ItemOtherDAO
) extends BaseController with I18nSupport {

  var ITEM_TYPE_FILTER = 0;
  var itemTypeList :Seq[ItemTypeOrder] = Seq.empty; // 仮設材種別

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    ITEM_TYPE_FILTER = 0
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
      val carList = carDAO.selectCarMasterInfo(placeId = placeId)

      /*仮設材種別取得*/
      itemTypeList = itemTypeDAO.selectItemCarInfoOrder(placeId);

      Ok(views.html.manage.itemCar(ITEM_TYPE_FILTER, carList, itemTypeList, placeId))
    }else {
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

  /** 登録・更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputCarId" -> text
      , "inputCarNo" -> text.verifying(Messages("error.manage.ItemCar.update.inputCarNo.empty"), {!_.isEmpty})
      , "inputCarBtxId" -> text.verifying(Messages("error.manage.ItemCar.update.inputCarBtxId.empty"), {_.matches("^[0-9]+$")})
                                    .verifying(Messages("error.manage.ItemCar.update.inputCarBtxId.empty"), {inputCarBtxId => inputCarBtxId != "0"})
      , "inputCarKeyBtxIdDsp" -> text
      , "inputCarKeyBtxId" -> text.verifying(Messages("error.manage.ItemCar.update.inputCarKeyBtxId.empty"), {_.matches("^[0-9]+$")})
                                       .verifying(Messages("error.manage.ItemCar.update.inputCarKeyBtxId.empty"), {inputCarKeyBtxId => inputCarKeyBtxId != "0"})
      , "inputCarTypeName" -> text
      , "inputCarTypeId" -> text.verifying(Messages("error.manage.ItemCar.update.inputCarTypeId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarTypeCategoryId" -> number
      , "inputCarName" -> text.verifying(Messages("error.manage.ItemCar.update.inputCarName.empty"), {!_.isEmpty})
      , "inputCarNote" -> text
    )(CarUpdateForm.apply)(CarUpdateForm.unapply))
    // 権限レベルを取得
    val reqIdentity = request.identity
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemCarController.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
      var errMsg = Seq[String]()
      val f = form.get
      // 鍵Tag IDが「無」の場合の対処
      var carKeyBtxId = f.inputCarKeyBtxId
      // カテゴリーが立馬(1)の場合、鍵TagIDに-1をセット
      if(f.inputCarTypeCategoryId == 1){
          carKeyBtxId = "-1"
      }
      // TagIDと鍵TagIDが同じIDを指定していないかチェック[
      if (f.inputCarBtxId.toInt == carKeyBtxId.toInt) {
        errMsg :+= Messages("error.manage.ItemCar.update.inputCarKey.duplicate", f.inputCarNo)
      }
      // 種別存在チェック
      val itemTypeList = itemTypeDAO.selectItemTypeCheck(f.inputCarTypeName, f.inputPlaceId.toInt)
      if (itemTypeList.isEmpty) {
        errMsg :+= Messages("error.manage.ItemCar.update.NotTypeName", f.inputCarNo)
      }
      if (errMsg.isEmpty == false) {
        // エラーで遷移
        Redirect(routes.ItemCarController.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      } else {
        if (f.inputCarId.isEmpty) {
          // 新規登録の場合 --------------------------
          // 作業車・立馬TxビーコンIDが存在しないか
          val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, f.inputCarBtxId.toInt)
          if (chkCarTagIdInf.length > 0) {
            errMsg :+= Messages("error.manage.ItemCar.update.inputCarBtxId.use", f.inputCarBtxId)
          }
          // その他仮設材管理にも存在してはいけない
          val itemOtherBtxList = itemOtherDAO.selectItemOtherBtxListBtxCheck(super.getCurrentPlaceId, f.inputCarBtxId.toInt)
          if (itemOtherBtxList.length > 0) {
            errMsg :+= Messages("error.manage.ItemCar.update.inputCarBtxId.useOther", f.inputCarBtxId.toInt)
          }
          if (carKeyBtxId.toInt > 0) {
            // 作業車・立馬鍵TxビーコンIDが存在しないか
            val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, carKeyBtxId.toInt)
            if (chkCarTagIdInf.length > 0) {
              errMsg :+= Messages("error.manage.ItemCar.update.inputCarKeyBtxId.use", carKeyBtxId)
            }
            // その他仮設材管理にも存在してはいけない
            val itemOtherBtxList = itemOtherDAO.selectItemOtherBtxListBtxCheck(super.getCurrentPlaceId, carKeyBtxId.toInt)
            if (itemOtherBtxList.length > 0) {
              errMsg :+= Messages("error.manage.ItemCar.update.inputCarKeyBtxId.useOther", carKeyBtxId.toInt)
            }
          }
          if (errMsg.isEmpty == false) {
            // エラーで遷移
            Redirect(routes.ItemCarController.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          } else {
            // DB処理
            carDAO.insert(
              f.inputCarNo
              , f.inputCarName
              , f.inputCarBtxId.toInt
              , carKeyBtxId.toInt
              , f.inputCarTypeId.toInt
              , f.inputCarNote
              , f.inputPlaceId.toInt)
            Redirect(routes.ItemCarController.index)
              .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.ItemCar.update"))
          }
        } else {
          // 更新の場合 --------------------------
          // 作業車・立馬番号重複チェック
          // 予約情報テーブルに作業車・立馬IDが存在していないか
          val itemCarReserveList = carDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.inputCarId.toInt, f.inputCarTypeId.toInt)
          if (itemCarReserveList.length > 0) {
            var chkFlg: Int = 0
            // 現在時刻設定
            val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
            val currentTime = new Date();
            val mTime = mSimpleDateFormat.format(currentTime)
            val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
            for (itemCarReserve <- itemCarReserveList) {
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
              errMsg :+= Messages("error.manage.ItemCar.update.inputCarIdReserve.use.noChange", f.inputCarId);
            } else if (chkFlg == 2) {
              if(reqIdentity.level < 3) {
                // 権限がレベル３以下のみ過去の予約情報が有る場合エラーにする
                errMsg :+= Messages("error.manage.ItemCar.update.inputCarIdReserve.use.exceed", f.inputCarId);
              }
            }
          }
          // TagIDがその他仮設材管理に存在してはいけない
          val itemOtherBtxList1 = itemOtherDAO.selectItemOtherBtxListBtxCheck(super.getCurrentPlaceId, f.inputCarBtxId.toInt)
          if (itemOtherBtxList1.length > 0) {
            errMsg :+= Messages("error.manage.ItemCar.update.inputCarBtxId.useOther", f.inputCarBtxId.toInt)
          }
          // 鍵tagIDがその他仮設材管理に存在してはいけない
          val itemOtherBtxList2 = itemOtherDAO.selectItemOtherBtxListBtxCheck(super.getCurrentPlaceId, carKeyBtxId.toInt)
          if (itemOtherBtxList2.length > 0) {
            errMsg :+= Messages("error.manage.ItemCar.update.inputCarKeyBtxId.useOther", carKeyBtxId.toInt)
          }
          // 変更前タグ情報
          val preCarInfo = carDAO.selectCarInfo(super.getCurrentPlaceId, "", Option(f.inputCarId.toInt)).last
          if (carKeyBtxId.toInt == preCarInfo.itemCarBtxId && f.inputCarBtxId.toInt == preCarInfo.itemCarKeyBtxId) {
            // 入力を逆転している場合は何もしない
          } else {
            // タグチェック
            val checkList = Seq[(Int, Int)](
              (preCarInfo.itemCarBtxId, f.inputCarBtxId.toInt)
              , (preCarInfo.itemCarKeyBtxId, carKeyBtxId.toInt)
            )
            checkList.zipWithIndex.foreach { case (btxId, i) =>
              if (btxId._1 == btxId._2) {
                // 変更前と同じの場合何もしない
              } else {
                // 登録が重複する場合
                if (carKeyBtxId.toInt > 0) {
                  if (i == 0) {
                    // TagID
                    if (f.inputCarBtxId.toInt == preCarInfo.itemCarKeyBtxId) {
                      // 作業車Txに変更前の鍵Txを登録する、且つ鍵Txが変更されている場合は何もしない
                    } else {
                      val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, carId = Option(f.inputCarId.toInt), f.inputCarBtxId.toInt)
                      if (chkCarTagIdInf.length > 0) {
                        errMsg :+= Messages("error.manage.ItemCar.update.inputCarBtxId.duplicate", btxId._2)
                      }
                    }
                  } else {
                    // 鍵TagID
                    if (carKeyBtxId.toInt == preCarInfo.itemCarBtxId) {
                      // 鍵Txに変更前の作業車Txを登録する場合は何もしない
                    } else {
                      val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, carId = Option(f.inputCarId.toInt), carKeyBtxId.toInt)
                      if (chkCarTagIdInf.length > 0) {
                        errMsg :+= Messages("error.manage.ItemCar.update.inputCarKeyBtxId.duplicate", btxId._2)
                      }
                    }
                  }
                } else {
                  if (i == 0) {
                    // TagID
                    if (f.inputCarBtxId.toInt == preCarInfo.itemCarKeyBtxId) {
                      // 作業車Txに変更前の鍵Txを登録する、且つ鍵Txが変更されている場合は何もしない
                    } else {
                      val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, carId = Option(f.inputCarId.toInt), f.inputCarBtxId.toInt)
                      if (chkCarTagIdInf.length > 0) {
                        errMsg :+= Messages("error.manage.ItemCar.update.inputCarBtxId.duplicate", btxId._2)
                      }
                    }
                  }
                }
              }
            }
          }
          if (errMsg.isEmpty == false) {
            // エラーで遷移
            Redirect(routes.ItemCarController.index)
              .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
          } else {
            // DB処理
            carDAO.update(
              f.inputCarId.toInt
              , f.inputCarNo
              , f.inputCarName
              , f.inputCarBtxId.toInt
              , carKeyBtxId.toInt
              , f.inputCarTypeId.toInt
              , f.inputCarNote
              , super.getCurrentPlaceId
              , preCarInfo.itemCarBtxId
              , preCarInfo.itemCarKeyBtxId
            )
            Redirect(routes.ItemCarController.index)
              .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.ItemCar.update"))
          }
        }
      }
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // 権限レベルを取得
    val reqIdentity = request.identity
    // フォームの準備
    var errMsg = Seq[String]()
    val inputForm = Form(mapping(
      "deleteCarId" -> text.verifying(Messages("error.manage.ItemCar.delete.empty"), {
        !_.isEmpty
      })
      ,"deleteCarTypeId" -> text
    )(CarDeleteForm.apply)(CarDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemCarController.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get

      // 予約情報テーブルに作業車・立馬IDが存在していないか
      val carReserveList = carDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.deleteCarId.toInt, f.deleteCarTypeId.toInt)
      if (carReserveList.length > 0) {
        var chkFlg: Int = 0
        // 現在時刻設定
        val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
        val currentTime = new Date();
        val mTime = mSimpleDateFormat.format(currentTime)
        val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
        for (itemCarReserve <- carReserveList) {
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
          errMsg :+= Messages("error.manage.ItemCar.delete.inputCarIdReserve.use.noChange", f.deleteCarId);
        } else if (chkFlg == 2) {
          if(reqIdentity.level < 3) {
            // 権限がレベル３以下のみ予約情報が有る場合エラーにする
            errMsg :+= Messages("error.manage.ItemCar.delete.inputCarIdReserve.use.exceed", f.deleteCarId);
          }
        }
      }

      // 検索
      val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, "", Option(f.deleteCarId.toInt))
      // タグ
      if (carList.length <= 0) {
        errMsg :+= Messages("error.manage.ItemCar.delete.inputCarId.empty", f.deleteCarId)
      }
      if(errMsg.nonEmpty) {
        // エラーで遷移
        Redirect(routes.ItemCarController.index)
          .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      }else{
        // 削除
        carDAO.delete(f.deleteCarId.toInt, super.getCurrentPlaceId)
        // リダイレクト
        Redirect(routes.ItemCarController.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.ItemCar.delete"))
      }
    }
  }
}
