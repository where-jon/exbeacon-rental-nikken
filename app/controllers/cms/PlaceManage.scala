package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
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
case class PlaceChangeForm(inputPlaceId: String)
case class PlaceRegisterForm(placeName: String)
case class PlaceUpdateForm(inputPlaceId: String, inputPlaceName: String, inputPlaceStatus: String)
case class PasswordUpdateForm(inputPlaceId: String, inputPassword: String, inputRePassword: String)
case class PlaceDeleteForm(deletePlaceId: String)
case class PlaceSortForm(placeSortId: String)

@Singleton
class PlaceManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                            , placeDAO: models.placeDAO
                            , floorDAO: models.floorDAO
                            , exbDAO: models.exbModelDAO
                            , passwordHasherRegistry: PasswordHasherRegistry
                               ) extends BaseController with I18nSupport {

  // FIXME: matsumura 実証実験では固定登録とする
  val BTX_API_URL = "https://demo6743258.mockable.io/getTakasagoBtx"

  /** 選択されている並び順 */
  var selectedSortType = 0

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    if(securedRequest2User.isSysMng){
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType))
    }else{
      // シス管でなければ登録されている現場の管理画面へ遷移
      Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${securedRequest2User.currentPlaceId.get}""")
    }
  }

  /** 指定順一覧表示 */
  def sortPlaceListWith(sortType:Int = 0) = SecuredAction { implicit request =>
    if(securedRequest2User.isSysMng){
      val placeList = placeDAO.selectPlaceListWithSortType(sortType)
      selectedSortType = sortType
      Ok(views.html.cms.placeManage(placeList))
    }else{
      // シス管でなければ登録されている現場の管理画面へ遷移
      Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${securedRequest2User.currentPlaceId.get}""")
    }
  }

  /** 詳細 */
  def change = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
    )(PlaceChangeForm.apply)(PlaceChangeForm.unapply))

    val form = inputForm.bindFromRequest
    val f = form.get

    // 現在の現場IDを更新
    placeDAO.updateCurrentPlaceId(f.inputPlaceId.toInt, securedRequest2User.id.get.toInt)

    Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")

  }
    /** 詳細 */
  def detail = SecuredAction { implicit request =>
    // 選択された現場の現場ID
    var placeId = securedRequest2User.currentPlaceId.get

    // 現場情報の取得
    val placeList = placeDAO.selectPlaceList(Seq[Int](placeId))
    // フロア情報の取得
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 現場状態の選択肢リスト
    val statusList = PlaceEnum().map

    if (placeList.isEmpty) {
      if(securedRequest2User.isSysMng) {
        // エラーメッセージ
        val errMsg = Messages("error.cms.placeManage.move.empty")
        // リダイレクトで画面遷移
        Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
      } else {
        // 対象の現場にアクセス不可＆システム管理者出ない場合はログアウト
        Redirect("/signout")
      }
    } else {
      // 画面遷移
      Ok(views.html.cms.placeManageDetail(placeList.last, floorInfoList, statusList, securedRequest2User.isSysMng))
    }
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
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      // DB登録
      placeDAO.insert(form.get.placeName, BTX_API_URL)

      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.register"))
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
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
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

  /** 更新 */
  def passwordUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputPassword" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputPassword.empty"), {!_.isEmpty})
      , "inputRePassword" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
    )(PasswordUpdateForm.apply)(PasswordUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get

      if(f.inputPassword != f.inputRePassword){
        Redirect(routes.PlaceManage.detail()).flashing(ERROR_MSG_KEY -> Messages("error.cms.PlaceManage.passwordUpdate.notEqual"))
      }else{
        // DB登録
        val passwordInfo = passwordHasherRegistry.current.hash(f.inputPassword)
        placeDAO.updatePassword(f.inputPlaceId.toInt, passwordInfo.password)

        Redirect(s"""${routes.PlaceManage.detail().path()}?${KEY_PLACE_ID}=${f.inputPlaceId}""")
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.passwordUpdate"))
      }



    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "deletePlaceId" -> text.verifying(Messages("error.cms.placeManage.delete.empty"), {!_.isEmpty})
    )(PlaceDeleteForm.apply)(PlaceDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      val f = form.get
      // DB処理
      placeDAO.deleteLogicalById(f.deletePlaceId.toInt)

      // 現場一覧の方にリダイレクト
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.delete"))
    }
  }
}
