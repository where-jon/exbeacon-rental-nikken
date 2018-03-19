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
case class InputForm(placeName: String)
case class PlaceUpdateForm(inputPlaceId: String, inputPlaceName: String, inputPlaceStatus: String)
case class PlaceDeleteForm(inputPlaceId: String)

@Singleton
class PlaceManage @Inject()(config: Configuration
                            , val silhouette: Silhouette[MyEnv]
                            , val messagesApi: MessagesApi
                            , placeDAO: models.placeDAO
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
    val placeIdStr = super.getRequestPlaceIdStr

    // 現場情報の取得
    val placeList = placeDAO.selectPlaceList(Seq[Int](placeIdStr.toInt))

    // 画面遷移＋現場IDをセッションに保存
    Ok(views.html.cms.placeManageDetail(placeList.last, PlaceEnum().map))
        .withSession(request.session + (CURRENT_PLACE_ID -> super.getRequestPlaceIdStr))
  }

  /** 登録 */
  def register = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "placeName" -> text.verifying(Messages("error.cms.PlaceManage.register.inputPlaceName.empty"), {!_.isEmpty})
    )(InputForm.apply)(InputForm.unapply))

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

      Redirect(routes.PlaceManage.detail().path() + s"""?${KEY_PLACE_ID}=${f.inputPlaceId}""")
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
      // DB登録
      placeDAO.deleteById(f.inputPlaceId.toInt)

      // 現場一覧の方にリダイレクト
      Redirect(routes.PlaceManage.index()).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.PlaceManage.delete"))
    }
  }

}
