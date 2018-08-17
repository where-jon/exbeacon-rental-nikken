package controllers.cms

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import javax.inject.{Inject, Singleton}
import models.{ItemType, ItemTypeOrder}
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

// フォームの定義
case class CarUpdateForm(
     inputPlaceId: String
   , inputCarId: String
   , inputCarNo: String
   , inputCarBtxId: String
   , inputCarKeyBtxId: String
   , inputCarTypeName: String
   , inputCarTypeId: String
   , inputCarName: String
   , inputCarNote: String
)

case class CarDeleteForm(
    deleteCarId: String
)

@Singleton
class ItemCarManage @Inject()(
  config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , carDAO: models.itemCarDAO
  , exbDAO: models.ExbDAO
  , itemTypeDAO:models.ItemTypeDAO
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

      Ok(views.html.cms.itemCarManage(ITEM_TYPE_FILTER, carList, itemTypeList, placeId))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 登録・更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputCarId" -> text
      , "inputCarNo" -> text.verifying(Messages("error.cms.CarManage.update.inputCarNo.empty"), {!_.isEmpty})
      , "inputCarBtxId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarBtxId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarKeyBtxId" -> text
      , "inputCarTypeName" -> text
      , "inputCarTypeId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarTypeId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarName" -> text.verifying(Messages("error.cms.CarManage.update.inputCarName.empty"), {!_.isEmpty})
      , "inputCarNote" -> text
    )(CarUpdateForm.apply)(CarUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemCarManage.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else {

      var errMsg = Seq[String]()
      val f = form.get

      // 鍵Tag IDが「無」の場合の対処
      var carKeyBtxId = f.inputCarKeyBtxId
      if (carKeyBtxId == "無" || carKeyBtxId.isEmpty) {
        carKeyBtxId = "-1"
      } else {
        if (!carKeyBtxId.matches("^[-0-9]+$")) {
          errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.empty")
        } else {
          if (f.inputCarBtxId == carKeyBtxId) {
            errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.inputCarKeyBtxId.duplicate", f.inputCarNo)
          }
        }
      }

      // 種別存在チェック
      val itemTypeList = itemTypeDAO.selectItemTypeCheck(f.inputCarTypeName, f.inputPlaceId.toInt)
      if (itemTypeList.isEmpty) {
        errMsg :+= Messages("error.cms.CarManage.update.NotTypeName", f.inputCarNo)
      }
      if (errMsg.isEmpty == false) {
        // エラーで遷移
        Redirect(routes.ItemCarManage.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      } else {

        if (f.inputCarId.isEmpty) {
          // 新規登録の場合 --------------------------
          // 作業車番号重複チェック
          val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, f.inputCarNo)
          //        if(carList.length > 0){
          //          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
          //        }

          // 作業車・立馬TxビーコンIDが存在しないか
          val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, f.inputCarBtxId.toInt)
          if (chkCarTagIdInf.length > 0) {
            errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.use", f.inputCarBtxId)
          }
          // 作業車・立馬鍵TxビーコンIDが存在しないか
          if (carKeyBtxId.toInt > 0) {
            val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, None, carKeyBtxId.toInt)
            if (chkCarTagIdInf.length > 0) {
              errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.use", carKeyBtxId)
            }
          }

          if (errMsg.isEmpty == false) {
            // エラーで遷移
            Redirect(routes.ItemCarManage.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
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

            Redirect(routes.ItemCarManage.index)
              .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.update"))
          }

        } else {
          // 更新の場合 --------------------------
          // 作業車・立馬番号重複チェック
          var carList = carDAO.selectCarInfo(super.getCurrentPlaceId, f.inputCarNo)
          carList = carList.filter(_.itemCarId != f.inputCarId.toInt).filter(_.itemCarNo == f.inputCarNo)
          //        if(carList.length > 0){
          //          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
          //        }

          // 予約情報テーブルに作業車・立馬IDが存在していないか
          val itemCarReserveList = carDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.inputCarId.toInt)
          if (itemCarReserveList.length > 0) {
            errMsg :+= Messages("error.cms.ItemOtherManage.update.inputItemOtherIdReserve.use.noChange", f.inputCarId)
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
                        errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.duplicate", btxId._2)
                      }
                    }
                  } else {
                    // 鍵TagID
                    if (carKeyBtxId.toInt == preCarInfo.itemCarBtxId) {
                      // 鍵Txに変更前の作業車Txを登録する場合は何もしない
                    } else {
                      val chkCarTagIdInf = carDAO.selectCarTagCheck(super.getCurrentPlaceId, carId = Option(f.inputCarId.toInt), carKeyBtxId.toInt)
                      if (chkCarTagIdInf.length > 0) {
                        errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.duplicate", btxId._2)
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
                        errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.duplicate", btxId._2)
                      }
                    }
                  }
                }
              }
            }
          }

          if (errMsg.isEmpty == false) {
            // エラーで遷移
            Redirect(routes.ItemCarManage.index)
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

            Redirect(routes.ItemCarManage.index)
              .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.update"))
          }
        }
      }
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    var errMsg = Seq[String]()
    val inputForm = Form(mapping(
      "deleteCarId" -> text.verifying(Messages("error.cms.CarManage.delete.empty"), {
        !_.isEmpty
      })
    )(CarDeleteForm.apply)(CarDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemCarManage.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get

      // 予約情報テーブルに作業車・立馬IDが存在していないか
      val carReserveList = carDAO.selectCarReserveCheck(super.getCurrentPlaceId, f.deleteCarId.toInt)
      if(carReserveList.length > 0){
        errMsg :+= Messages("error.cms.CarManage.delete.inputCarIdReserve.use", f.deleteCarId)
      }

      // 検索
      val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, "", Option(f.deleteCarId.toInt))
      // タグ
      if (carList.length <= 0) {
        errMsg :+= Messages("error.cms.CarManage.delete.inputCarId.empty", f.deleteCarId)
      }
      if(errMsg.nonEmpty) {
        // エラーで遷移
        Redirect(routes.ItemCarManage.index)
          .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      }else{
        // 削除
        carDAO.delete(f.deleteCarId.toInt, super.getCurrentPlaceId)
        // リダイレクト
        Redirect(routes.ItemCarManage.index)
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.delete"))
      }
    }
  }
}
