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
  * 現場管理アクションクラス
  *
  *
  */

// フォーム定義
case class PlaceRegisterForm(placeName: String)
case class PlaceUpdateForm(inputPlaceId: String, inputPlaceName: String, inputPlaceStatus: String)
case class PlaceDeleteForm(inputPlaceId: String)
case class FloorDeleteForm(inputPlaceId: String, inputFloorId: String)
case class FloorUpdateForm(inputPlaceId: String, inputFloorId: String, inputFloorName: String, inputExbDeviceIdListComma: String)

@Singleton
class PlaceManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                            , placeDAO: models.placeDAO
                            , floorDAO: models.floorDAO
                            , exbDAO: models.exbModelDAO
                               ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 現場一覧を表示＋セッションをクリア
    val placeList = placeDAO.selectPlaceList()
    Ok(views.html.cms.placeManage(placeList))
      .withSession(request.session + (CURRENT_PLACE_ID -> ""))
  }

  /** 詳細 */
  def detail = SecuredAction { implicit request =>
    // 選択された現場の現場ID
    var placeIdStr = super.getRequestPlaceIdStr
    if(placeIdStr.isEmpty){
      placeIdStr = request.session.get(CURRENT_PLACE_ID).map { str =>
        str
      }.getOrElse {
        securedRequest2User.placeId.get.toString
      }
    }

    // 現場情報の取得
    val placeList = placeDAO.selectPlaceList(Seq[Int](placeIdStr.toInt))
    // フロア情報の取得
    val floorInfoList = floorDAO.selectFloorInfo(placeIdStr.toInt)
    // 現場状態の選択肢リスト
    val statusList = PlaceEnum().map

    // 画面遷移＋現場IDをセッションに保存
    Ok(views.html.cms.placeManageDetail(placeList.last, floorInfoList, statusList))
        .withSession(request.session + (CURRENT_PLACE_ID -> super.getRequestPlaceIdStr))
  }

  /** 登録 */
  def register = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "placeName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty})
    )(PlaceRegisterForm.apply)(PlaceRegisterForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString("<br/>")
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.index()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      // DB登録
      placeDAO.insert(form.get.placeName)

      Redirect(routes.PlaceManage.index()).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.register"))
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputPlaceName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty})
      , "inputPlaceStatus" -> text
    )(PlaceUpdateForm.apply)(PlaceUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString("<br/>")
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get
      // DB登録
      placeDAO.updateById(f.inputPlaceId.toInt, f.inputPlaceName, f.inputPlaceStatus.toInt)

      Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.update"))
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
    )(PlaceDeleteForm.apply)(PlaceDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString("<br/>")
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get
      // DB処理
      placeDAO.deleteById(f.inputPlaceId.toInt)

      // 現場一覧の方にリダイレクト
      Redirect(routes.PlaceManage.index()).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.delete"))
    }
  }

  /** フロア更新 */
  def floorUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputFloorId" -> text
      , "inputFloorName" -> text.verifying(Messages("error.cms.PlaceManage.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
      , "inputExbDeviceIdListComma" -> text.verifying(Messages("error.cms.PlaceManage.floorUpdate.inputExbDeviceIdListComma.empty"), {!_.isEmpty})
    )(FloorUpdateForm.apply)(FloorUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString("<br/>")
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      var errMsg = Seq[String]()
      val f = form.get
      if(f.inputFloorId.isEmpty){
        // 新規フロア登録の場合 --------------------------

        // フロア名称重複チェック
        val floorList = floorDAO.selectFloorInfo(f.inputPlaceId.toInt, f.inputFloorName)
        if(floorList.length > 0){
          errMsg :+= Messages("error.cms.PlaceManage.floorUpdate.inputFloorName.duplicate")
        }
        // デバイスID重複チェック
        val exbDeviceIdList = exbDAO.selectExb(f.inputPlaceId.toInt).map(exb =>{exb.exbDeviceId})
        val inputExbDeviceIdList = f.inputExbDeviceIdListComma.split(",").toSeq
        val errDeviceIdList = inputExbDeviceIdList.map(deviceId => {
          if(exbDeviceIdList.contains(deviceId)){
            deviceId
          }
        })
        if(errDeviceIdList.isEmpty == false){
          errMsg :+= Messages("error.cms.PlaceManage.floorUpdate.inputFloorName.duplicate", errDeviceIdList.mkString(","))
        }
        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(ERROR_MSG_KEY -> errMsg.mkString("<br/>"))
        }else{
          // DB処理
          floorDAO.insert(f.inputFloorName, f.inputPlaceId.toInt, inputExbDeviceIdList)
          // 成功で遷移
          Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.floorUpdate"))
        }

      }else{
        // フロア更新の場合 --------------------------


        // フロア名称重複チェック
        val floorList = floorDAO.selectFloorInfo(f.inputPlaceId.toInt)



        Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> errMsg.mkString("<br/>"))
      }
    }

  }


  /** フロア削除 */
  def floorDelete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputFloorId" -> text
    )(FloorDeleteForm.apply)(FloorDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    val f = form.get
    // DB処理
    floorDAO.deleteById(f.inputFloorId.toInt)

    // リダイレクト
    Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
      .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.floorDelete"))
  }


}
