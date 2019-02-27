package controllers.tenant

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import controllers.{BaseController, site}
import javax.inject.{Inject, Singleton}
import models._
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

@Singleton
class RegionController @Inject() (
  config: Configuration,
  val silhouette: Silhouette[MyEnv],
  val messagesApi: MessagesApi,
  placeDAO: models.placeDAO,
  floorDAO: models.floorDAO,
  exbDAO: models.manage.ExbDAO,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService,
  companyDAO: models.companyDAO,
  itemTypeDAO: models.ItemTypeDAO,
  itemOtherDAO: models.itemOtherDAO,
  itemCarDAO: models.itemCarDAO
) extends BaseController with I18nSupport {

  /** 選択されている並び順 */
  var sortType = 0

  /** リージョン初期表示 */
  def region = SecuredAction { implicit request =>
    val identity = request.identity
    if (identity.level >= 4) {
      if (securedRequest2User.isSysMng) {
        Redirect(routes.RegionController.regionList(sortType))
      } else {
        // シス管でなければ登録されている現場の管理画面へ遷移
        if (super.isCmsLogged) {
          Redirect(routes.RegionController.regionDetail)
        } else {
          Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
        }
      }
    } else {
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

  /** リージョン指定順一覧表示 */
  def regionList(sortType:Int) = SecuredAction { implicit request =>
    val identity = request.identity
    if (identity.level >= 4) {
      if (securedRequest2User.isSysMng) {
        val placeList = placeDAO.selectPlaceListWithSortTypeEx(sortType)
        val statusList = PlaceEnum().map
        Ok(views.html.tenant.region(placeList, statusList))
      } else {
        if (super.isCmsLogged) {
          // シス管でなければ登録されている現場の管理画面へ遷移
          Redirect(routes.RegionController.regionDetail)
        } else {
          Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
        }
      }
    }else {
      Redirect(site.routes.ItemCarMaster.index)
    }
  }


  /** リージョン詳細 */
  def regionDetail = SecuredAction { implicit request =>
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
          Redirect(routes.RegionController.regionList(sortType)).flashing(ERROR_MSG_KEY -> errMsg)
        } else {
          // 対象の現場にアクセス不可＆システム管理者出ない場合はログアウト
          Redirect("/signout")
        }
      } else {
        // 画面遷移
        Ok(views.html.tenant.regionDetail(placeList.last, statusList, securedRequest2User.isSysMng))
      }
    }else{
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

  /** リージョン登録 */
  def regionRegister = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(
      mapping(
        "placeName" -> text.verifying(Messages("error.tenanat.Region.register.inputPlaceName.empty"), {!_.isEmpty}),
        "placeStatus" -> text.verifying(Messages("error.tenanat.Region.register.inputPlaceStatus.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.tenanat.Region.register.inputUserLoginId.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.tenanat.Region.register.inputUserName.empty"), {!_.isEmpty}),
        "userPassword1" -> text.verifying(Messages("error.tenanat.Region.passwordUpdate.inputPassword.empty"), {!_.isEmpty}),
        "userPassword2" -> text.verifying(Messages("error.tenanat.Region.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
      )
      (PlaceRegisterForm.apply)
      (PlaceRegisterForm.unapply)
      verifying (
        Messages("error.tenanat.Region.passwordUpdate.notEqual"),
        form => form.userPassword1 == form.userPassword2
      )
      verifying (
        Messages("error.tenanat.Region.register.inputUserLoginId.exist"), // 指定されたログインIDは既に使われています。
        form => (userService.selectByLoginId(form.userLoginId).length == 0)
      )
    )
    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.RegionController.regionList(sortType)).flashing(ERROR_MSG_KEY -> errMsg)
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
      Redirect(routes.RegionController.regionList(sortType))
        .flashing(SUCCESS_MSG_KEY -> Messages("success.tenanat.Region.register"))
    }
  }

   /** リージョン管理現場変更 */
  def regionChange = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
    )(PlaceChangeForm.apply)(PlaceChangeForm.unapply))

    val form = inputForm.bindFromRequest
    val getForm = form.get

    // 現在の現場IDを更新
    placeDAO.updateCurrentPlaceId(getForm.inputPlaceId.toInt, securedRequest2User.id.get.toInt)

    Redirect(routes.RegionController.regionDetail)
  }

  /** リージョン更新 */
  def regionUpdate = SecuredAction { implicit request =>
    val inputForm = Form(mapping(
        "placeId" -> text,
        "userId" -> text,
        "placeName" -> text.verifying(Messages("error.tenanat.Region.register.inputPlaceName.empty"), {!_.isEmpty}),
        "placeStatus" -> text.verifying(Messages("error.tenanat.Region.register.inputPlaceStatus.empty"), {!_.isEmpty}),
        "userName" -> text.verifying(Messages("error.tenanat.Region.register.inputUserName.empty"), {!_.isEmpty}),
        "userLoginId" -> text.verifying(Messages("error.tenanat.Region.register.inputUserLoginId.empty"), {!_.isEmpty})
    )(PlaceUpdateForm.apply)(PlaceUpdateForm.unapply)
      verifying (
      Messages("error.tenanat.Region.register.inputUserLoginId.exist"), // 指定されたログインIDは既に使われています。
      form => (userService.checkExistByLoginId(form.userLoginId, form.userId.toInt).length == 0)
    )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      Redirect(routes.RegionController.regionDetail())
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      placeDAO.updateById(form.get.placeId.toInt, form.get.placeName, form.get.placeStatus.toInt)
      userService.updateUserNameById(form.get.userId.toInt, form.get.userLoginId, form.get.userName)
      Redirect(s"""${routes.RegionController.regionDetail().path()}?${KEY_PLACE_ID}=${form.get.placeId}""")
        .flashing(SUCCESS_MSG_KEY -> Messages("success.tenanat.Region.register"))
    }
  }


  /**
    * リージョンの削除
    */
  def regionDelete = SecuredAction { implicit request =>
    // POSTされたFORMを取得
    val inputForm = Form(mapping(
      "deletePlaceId" -> text.verifying(Messages("error.cms.placeManage.delete.empty"), {
        !_.isEmpty
      })
    )(PlaceDeleteForm.apply)(PlaceDeleteForm.unapply)
      verifying(
        Messages("error.tenanat.Region.noDelete.byAccount"), // この現場に紐づく現場アカウントが存在しているので削除できません。
        inForm => (userService.selectAccountByPlaceId(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byCompany"), // この現場に紐づく業者が存在しているので削除できません。
        inForm => (companyDAO.selectCompany(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byItemType"), // この現場に紐づく仮設材種別が存在しているので削除できません。
        inForm => (itemTypeDAO.selectItemTypeInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byOtherMaster"), // この現場に紐づくその他仮設材が存在しているので削除できません。
        inForm => (itemOtherDAO.selectOtherMasterInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byCarMaster"), // この現場に紐づく作業車・立馬が存在しているので削除できません。
        inForm => (itemCarDAO.selectCarMasterInfo(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byExb"), // この現場に紐づくEXBが存在しているので削除できません。
        inForm => (exbDAO.selectExbAll(inForm.deletePlaceId.toInt).length == 0)
      )
      verifying(
        Messages("error.tenanat.Region.noDelete.byFloor"), // この現場に紐づくフロアが存在しているので削除できません。
        inForm => (floorDAO.selectFloor(inForm.deletePlaceId.toInt).length == 0)
      )
    )
    val form = inputForm.bindFromRequest
    if (form.hasErrors) { // 入力内容がエラーなら中止
      Redirect(routes.RegionController.regionList(sortType))
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    } else {
      // 担当者アカウントを削除
      placeDAO.deleteLogicalById(form.get.deletePlaceId.toInt)
      // 現場を削除
      userService.deleteLogicalByPlaceId(form.get.deletePlaceId.toInt)
      // 画面遷移
      Redirect(routes.RegionController.regionList(sortType))
        .flashing(SUCCESS_MSG_KEY -> Messages("success.tenanat.Region.delete"))
    }
  }

  /** リージョンパスワード更新 */
  def regionPasswordUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "placeId" -> text,
        "userId" -> text,
        "password1" -> text.verifying(Messages("error.tenanat.Region.passwordUpdate.inputPassword.empty"), {!_.isEmpty})
      , "password2" -> text.verifying(Messages("error.tenanat.Region.passwordUpdate.inputRePassword.empty"), {!_.isEmpty})
    )(PasswordUpdateForm.apply)(PasswordUpdateForm.unapply))
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      Redirect(routes.RegionController.regionDetail()).flashing(ERROR_MSG_KEY -> errMsg)
    }else{
      if(form.get.password1 != form.get.password2){
        Redirect(routes.RegionController.regionDetail())
          .flashing(ERROR_MSG_KEY -> Messages("error.tenanat.Region.passwordUpdate.notEqual"))
      }else{
        userService.changePasswordById(
          form.get.userId,
          passwordHasherRegistry.current.hash(form.get.password1).password
        )
        Redirect(s"""${routes.RegionController.regionDetail().path()}?${KEY_PLACE_ID}=${form.get.placeId}""")
          .flashing(SUCCESS_MSG_KEY -> Messages("success.tenanat.Region.passwordUpdate"))
      }
    }
  }

   /** リージョン管理ページログイン */
  def regionLogin = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      ,"inputCmsLoginPassword" -> text
      ,"inputReturnPath" -> text
    )(CmsLoginForm.apply)(CmsLoginForm.unapply))

    val form = inputForm.bindFromRequest
    val getForm = form.get

    if(placeDAO.isExist(getForm.inputPlaceId.toInt, getForm.inputCmsLoginPassword)){
      // 詳細画面にセッションを付与して遷移
      Redirect(routes.RegionController.regionDetail).withSession(request.session + (CMS_LOGGED_SESSION_KEY -> "yes"))
    }else{
      // 元来た画面に遷移
      Redirect(getForm.inputReturnPath).flashing(ERROR_MSG_KEY -> Messages("error.tenanat.Region.cmsLogin"))
    }
  }
}
