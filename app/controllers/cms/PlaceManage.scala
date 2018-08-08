package controllers.cms

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import controllers.BaseController
import controllers.site
import models.{PlaceEnum, PlaceEx, User}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.{MyEnv, UserService}


/**
  * 現場管理アクションクラス
  *
  *
  */

// フォーム定義
case class PlaceChangeForm(inputPlaceId: String)
case class CmsLoginForm(inputPlaceId: String, inputCmsLoginPassword: String, inputReturnPath:String)
case class PlaceRegisterForm(
  placeName: String, // 現場名
  placeUserId: String, // 現場担当者ID
  placeUserName: String, // 現場担当者名
  placeUserPassword1: String, // パスワード
  placeUserPassword2: String // 確認用パスワード
)
case class PlaceUpdateForm(inputPlaceId: String, inputPlaceName: String, inputPlaceStatus: String)
case class PasswordUpdateForm(inputPlaceId: String, inputPassword: String, inputRePassword: String)
case class PlaceDeleteForm(deletePlaceId: String)
case class PlaceSortForm(placeSortId: String)

@Singleton
class PlaceManage @Inject() (
  config: Configuration,
  val silhouette: Silhouette[MyEnv],
  val messagesApi: MessagesApi,
  placeDAO: models.placeDAO,
  floorDAO: models.floorDAO,
  exbDAO: models.exbModelDAO,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService
) extends BaseController with I18nSupport {

  /** 選択されている並び順 */
  var selectedSortType = 0

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if (reqIdentity.level >= 4) {
      if (securedRequest2User.isSysMng) {
        Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType))
      } else {
        // シス管でなければ登録されている現場の管理画面へ遷移
        if (super.isCmsLogged) {
          Redirect(routes.PlaceManage.detail)
        } else {
          Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
        }
      }
    } else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 指定順一覧表示 */
  def sortPlaceListWith(sortType:Int = 0) = SecuredAction { implicit request =>
    if (securedRequest2User.isSysMng) {
      val placeList = placeDAO.selectPlaceListWithSortTypeEx(sortType)
      selectedSortType = sortType
      Ok(views.html.cms.placeManage(placeList))
    } else {
      if (super.isCmsLogged) {
        // シス管でなければ登録されている現場の管理画面へ遷移
        Redirect(routes.PlaceManage.detail)
      } else {
        Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
      }
    }
  }

  /** 管理現場変更 */
  def change = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
    )(PlaceChangeForm.apply)(PlaceChangeForm.unapply))

    val form = inputForm.bindFromRequest
    val f = form.get

    // 現在の現場IDを更新
    placeDAO.updateCurrentPlaceId(f.inputPlaceId.toInt, securedRequest2User.id.get.toInt)

    Redirect(routes.PlaceManage.detail)
  }

  /** 管理ページログイン */
  def cmsLogin = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      ,"inputCmsLoginPassword" -> text
      ,"inputReturnPath" -> text
    )(CmsLoginForm.apply)(CmsLoginForm.unapply))

    val form = inputForm.bindFromRequest
    val f = form.get

    if(placeDAO.isExist(f.inputPlaceId.toInt, f.inputCmsLoginPassword)){
      // 詳細画面にセッションを付与して遷移
      Redirect(routes.PlaceManage.detail).withSession(request.session + (CMS_LOGGED_SESSION_KEY -> "yes"))
    }else{
      // 元来た画面に遷移
      Redirect(f.inputReturnPath).flashing(ERROR_MSG_KEY -> Messages("error.cms.PlaceManage.cmsLogin"))
    }
  }


  /** 詳細 */
  def detail = SecuredAction { implicit request =>
    if(super.isCmsLogged){
      // 選択された現場の現場ID
      val placeId = securedRequest2User.currentPlaceId.get

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
    }else{
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

//  placeName: String, // 現場名
//  placeUserId: String, // 現場担当者ID
//  placeUserName: String, // 現場担当者名
//  placeUserPassword1: String, // パスワード
//  placeUserPassword2: String // 確認用パスワード

  /** 登録 */
  def register = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(
      mapping(
        "placeName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty}),
        "placeUserId" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceUserId.empty"), {!_.isEmpty}),
        "placeUserName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceUserName.empty"), {!_.isEmpty}),
        "placeUserPassword1" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputPassword.empty"), {!_.isEmpty}),
        "placeUserPassword2" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
      )
      (PlaceRegisterForm.apply)
      (PlaceRegisterForm.unapply)
      verifying (
        Messages("error.cms.PlaceManage.passwordUpdate.notEqual"),
        form => form.placeUserPassword1 == form.placeUserPassword2
      )
    )

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      // DB登録
      val placeEx = PlaceEx.apply(0, form.get.placeName, 0, 0, "", "", "", "", "", "", "")
      val placeId = placeDAO.insertEx(placeEx)
      System.out.println("new placeId=" + placeId)
      val hs = passwordHasherRegistry.current.hash(form.get.placeUserPassword1)
      System.out.println("hs=" + hs.password)
      val user = User.apply(
        Option(0),
        form.get.placeUserId,
        true,
        hs.password,
        form.get.placeUserName,
        Option(placeId),
        Option(placeId),
        true,
        3,
        null
      )
      userService.insert(user)

      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.register"))
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputPlaceName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty})
      , "inputPlaceStatus" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceStatus.empty"), {!_.isEmpty})
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
        placeDAO.updatePassword(f.inputPlaceId.toInt, f.inputPassword)

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
      userService.deleteLogicalByPlaceId(f.deletePlaceId.toInt)

      // 現場一覧の方にリダイレクト
      Redirect(routes.PlaceManage.sortPlaceListWith(selectedSortType)).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.delete"))
    }
  }
}
