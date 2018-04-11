package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.PlaceEnum
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * フロア管理アクションクラス
  *
  *
  */

// フォーム定義
case class FloorDeleteForm(deleteFloorId: String)
case class FloorUpdateForm(inputPlaceId: String, inputFloorId: String, inputFloorName: String, inputExbDeviceIdListComma: String)

@Singleton
class FloorManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                            , placeDAO: models.placeDAO
                            , floorDAO: models.floorDAO
                            , exbDAO: models.exbModelDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 選択されている現場の現場ID
    var placeId = securedRequest2User.currentPlaceId.get

    // 現場情報の取得
    val placeList = placeDAO.selectPlaceList(Seq[Int](placeId))
    // フロア情報の取得
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 現場状態の選択肢リスト
    val statusList = PlaceEnum().map

    if (placeList.isEmpty) {
      // 管理可能な情報が無ければログアウト
      Redirect("/signout")
    } else {
      // 画面遷移
      Ok(views.html.cms.floorManage(placeList.last, floorInfoList, statusList))
    }
  }

  /** フロア更新 */
  def floorUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputFloorId" -> text
      , "inputFloorName" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
      , "inputExbDeviceIdListComma" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.inputExbDeviceIdListComma.empty"), {!_.isEmpty})
    )(FloorUpdateForm.apply)(FloorUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      var errMsg = Seq[String]()
      val f = form.get
      if(f.inputFloorId.isEmpty){
        // 新規フロア登録の場合 --------------------------

        // フロア名称重複チェック
        val floorList = floorDAO.selectFloorInfo(f.inputPlaceId.toInt, f.inputFloorName)
        if(floorList.nonEmpty){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputFloorName.duplicate")
        }

        // デバイスID重複チェック
        val exbDeviceIdList = exbDAO.selectExb(f.inputPlaceId.toInt).map(exb =>{exb.exbDeviceId})
        val inputExbDeviceIdList = f.inputExbDeviceIdListComma.split("-").filter(_.isEmpty == false).toSeq

        if(inputExbDeviceIdList.exists(!_.matches("^[0-9]+$"))){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.notNumeric")
        } else {
          val errDeviceIdList = inputExbDeviceIdList.filter(exbDeviceIdList.contains(_))
          if(errDeviceIdList.nonEmpty) {
            errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.duplicate", errDeviceIdList.mkString(","))
          }
        }

        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(s"""${routes.FloorManage.index().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          floorDAO.insert(f.inputFloorName, f.inputPlaceId.toInt, inputExbDeviceIdList)
          // 成功で遷移
          Redirect(s"""${routes.FloorManage.index().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorUpdate"))
        }

      }else{
        // フロア更新の場合 --------------------------

        // フロア名称重複チェック
        val floorList = floorDAO.selectFloorInfo(f.inputPlaceId.toInt)
        val rest = floorList.filter(_.floorId != f.inputFloorId.toInt).filter(_.floorName == f.inputFloorName)
        if(rest.nonEmpty){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputFloorName.duplicate")
        }

        // デバイスID重複チェック
        val exbDeviceIdList = exbDAO.selectExb(f.inputPlaceId.toInt).filter(_.floorId != f.inputFloorId.toInt).map(exb =>{exb.exbDeviceId})
        val inputExbDeviceIdList = f.inputExbDeviceIdListComma.split("-").toSeq
        val errDeviceIdList = inputExbDeviceIdList.filter(exbDeviceIdList.contains(_))
        if(inputExbDeviceIdList.exists(!_.matches("^[0-9]+$"))){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.notNumeric")
        } else {
          val errDeviceIdList = inputExbDeviceIdList.filter(exbDeviceIdList.contains(_))
          if(errDeviceIdList.nonEmpty) {
            errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.duplicate", errDeviceIdList.mkString(","))
          }
        }


        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(s"""${routes.FloorManage.index().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          var placeId = securedRequest2User.currentPlaceId.get
          // DB処理
          floorDAO.updateById(placeId, f.inputFloorId.toInt, f.inputFloorName, inputExbDeviceIdList)
          // 成功で遷移
          Redirect(s"""${routes.FloorManage.index().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorUpdate"))
        }
      }
    }
  }

  /** フロア削除 */
  def floorDelete = SecuredAction { implicit request =>
    // フォームの準備
    val deleteForm = Form(mapping(
        "deleteFloorId" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.delete.empty"), {!_.isEmpty})
    )(FloorDeleteForm.apply)(FloorDeleteForm.unapply))

    // フォームの取得
    val form = deleteForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.FloorManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else {
      val f = form.get
      val placeId = securedRequest2User.currentPlaceId.get

      // DB処理
      floorDAO.deleteById(f.deleteFloorId.toInt)
      // リダイレクト
      Redirect(s"""${routes.FloorManage.index().path()}?${KEY_PLACE_ID}=${placeId}""")
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorDelete"))
    }
  }
}
