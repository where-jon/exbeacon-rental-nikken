package controllers.tenant

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import controllers.{BaseController, site}
import javax.inject.{Inject, Singleton}
import models.{PlaceEnum, PlaceEx, User}
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
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
  placeStatus: String, // 状態
  userLoginId: String, // 現場責任者ログインID
  userName: String, // 現場責任者名
  userPassword1: String, // パスワード
  userPassword2: String // 確認用パスワード
)
case class PlaceUpdateForm(
  placeId: String,
  userId: String,
  placeName: String,
  placeStatus: String,
  userName: String,
  userLoginId: String
)
case class PasswordUpdateForm(
  placeId: String,
  userId: String,
  password1: String,
  password2: String
)
case class PlaceDeleteForm(deletePlaceId: String)
case class PlaceSortForm(placeSortId: String)

@Singleton
class WorkPlaceController @Inject() (
  config: Configuration,
  val silhouette: Silhouette[MyEnv],
  val messagesApi: MessagesApi,
  placeDAO: models.placeDAO,
  floorDAO: models.floorDAO,
  exbDAO: models.ExbDAO,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService,
  companyDAO: models.companyDAO,
  itemTypeDAO: models.ItemTypeDAO,
  itemOtherDAO: models.itemOtherDAO,
  itemCarDAO: models.itemCarDAO
) extends BaseController with I18nSupport {

  /** 選択されている並び順 */
  var selectedSortType = 0

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if (reqIdentity.level >= 4) {
      if (securedRequest2User.isSysMng) {
        Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType))
      } else {
        // シス管でなければ登録されている現場の管理画面へ遷移
        if (super.isCmsLogged) {
          Redirect(routes.WorkPlaceController.detail)
        } else {
          Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
        }
      }
    } else {
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

  /** 指定順一覧表示 */
  def sortPlaceListWith(sortType:Int) = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if (reqIdentity.level >= 4) {
      if (securedRequest2User.isSysMng) {
        val placeList = placeDAO.selectPlaceListWithSortTypeEx(sortType)
        val statusList = PlaceEnum().map
        Ok(views.html.tenant.workPlace(placeList, statusList))
      } else {
        if (super.isCmsLogged) {
          // シス管でなければ登録されている現場の管理画面へ遷移
          Redirect(routes.WorkPlaceController.detail)
        } else {
          Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
        }
      }
    }else {
      Redirect(site.routes.ItemCarMaster.index)
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

    Redirect(routes.WorkPlaceController.detail)
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
      Redirect(routes.WorkPlaceController.detail).withSession(request.session + (CMS_LOGGED_SESSION_KEY -> "yes"))
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
      val placeList = placeDAO.selectPlaceExList(Seq[Int](placeId))
      // 現場状態の選択肢リスト
      val statusList = PlaceEnum().map

      if (placeList.isEmpty) {
        if(securedRequest2User.isSysMng) {
          // エラーメッセージ
          val errMsg = Messages("error.cms.placeManage.move.empty")
          // リダイレクトで画面遷移
          Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
        } else {
          // 対象の現場にアクセス不可＆システム管理者出ない場合はログアウト
          Redirect("/signout")
        }
      } else {
        // 画面遷移
        Ok(views.html.tenant.workPlaceDetail(placeList.last, statusList, securedRequest2User.isSysMng))
      }
    }else{
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

  /** 登録 */
  def register = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(
      mapping(
        "placeName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty}),
        "placeStatus" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceStatus.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.cms.PlaceManage.register.inputUserLoginId.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputUserName.empty"), {!_.isEmpty}),
        "userPassword1" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputPassword.empty"), {!_.isEmpty}),
        "userPassword2" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
      )
      (PlaceRegisterForm.apply)
      (PlaceRegisterForm.unapply)
      verifying (
        Messages("error.cms.PlaceManage.passwordUpdate.notEqual"),
        form => form.userPassword1 == form.userPassword2
      )
      verifying (
        Messages("error.cms.PlaceManage.register.inputUserLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.selectByLoginId(form.userLoginId).length == 0)
      )
    )
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType)).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      // DB登録
      val placeEx = PlaceEx.apply(0, form.get.placeName, 0, form.get.placeStatus.toInt, "", "", "", "", "", -1, "", "")
      val placeId = placeDAO.insertEx(placeEx)
      val hs = passwordHasherRegistry.current.hash(form.get.userPassword1)
      val user = User.apply(
        Option(0), form.get.userLoginId, true, hs.password, form.get.userName,
        Option(placeId), Option(placeId), "",
        true, 3, null
      )
      userService.insert(user)
      Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType))
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.register"))
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>
    val inputForm = Form(mapping(
        "placeId" -> text,
        "userId" -> text,
        "placeName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty}),
        "placeStatus" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceStatus.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputUserName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.cms.PlaceManage.register.inputUserLoginId.empty"), {!_.isEmpty})
    )(PlaceUpdateForm.apply)(PlaceUpdateForm.unapply)
      verifying (
      Messages("error.cms.PlaceManage.register.inputUserLoginId.exist"), // 指定されたログインIDは既に使われています。
      form => (userService.checkExistByLoginId(form.userLoginId, form.userId.toInt).length == 0)
    )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.WorkPlaceController.detail())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      placeDAO.updateById(form.get.placeId.toInt, form.get.placeName, form.get.placeStatus.toInt)
      userService.updateUserNameById(form.get.userId.toInt, form.get.userLoginId, form.get.userName)
      Redirect(s"""${routes.WorkPlaceController.detail().path()}?${KEY_PLACE_ID}=${form.get.placeId}""")
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.register"))
    }
  }

  /** パスワード更新 */
  def passwordUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "placeId" -> text,
        "userId" -> text,
        "password1" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputPassword.empty"), {!_.isEmpty})
      , "password2" -> text.verifying(Messages("error.cms.PlaceManage.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
    )(PasswordUpdateForm.apply)(PasswordUpdateForm.unapply))
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.WorkPlaceController.detail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      if(form.get.password1 != form.get.password2){
        Redirect(routes.WorkPlaceController.detail())
          .flashing(ERROR_MSG_KEY -> Messages("error.cms.PlaceManage.passwordUpdate.notEqual"))
      }else{
        userService.changePasswordById(
          form.get.userId,
          passwordHasherRegistry.current.hash(form.get.password1).password
        )
        Redirect(s"""${routes.WorkPlaceController.detail().path()}?${KEY_PLACE_ID}=${form.get.placeId}""")
          .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.passwordUpdate"))
      }
    }
  }

  /**
    * 現場の削除
    */
  def delete = SecuredAction { implicit request =>
    // POSTされたFORMを取得
    val inputForm = Form(mapping(
      "deletePlaceId" -> text.verifying(Messages("error.cms.placeManage.delete.empty"), {
        !_.isEmpty
      })
    )(PlaceDeleteForm.apply)(PlaceDeleteForm.unapply)
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byAccount"), // この現場に紐づく現場アカウントが存在しているので削除できません。
        inForm => (userService.selectAccountByPlaceId(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byCompany"), // この現場に紐づく業者が存在しているので削除できません。
        inForm => (companyDAO.selectCompany(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byItemType"), // この現場に紐づく仮設材種別が存在しているので削除できません。
        inForm => (itemTypeDAO.selectItemTypeInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byOtherMaster"), // この現場に紐づくその他仮設材が存在しているので削除できません。
        inForm => (itemOtherDAO.selectOtherMasterInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byCarMaster"), // この現場に紐づく作業車・立馬が存在しているので削除できません。
        inForm => (itemCarDAO.selectCarMasterInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byExb"), // この現場に紐づくEXBが存在しているので削除できません。
        inForm => (exbDAO.selectExbAll(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.cms.PlaceManage.noDelete.byFloor"), // この現場に紐づくフロアが存在しているので削除できません。
        inForm => (floorDAO.selectFloor(inForm.deletePlaceId.toInt).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) { // 入力内容がエラーなら中止
      Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType))
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
      // 担当者アカウントを削除
      placeDAO.deleteLogicalById(form.get.deletePlaceId.toInt)
      // 現場を削除
      userService.deleteLogicalByPlaceId(form.get.deletePlaceId.toInt)
      // 画面遷移
      Redirect(routes.WorkPlaceController.sortPlaceListWith(selectedSortType))
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.delete"))
    }
  }
}
