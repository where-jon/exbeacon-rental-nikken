package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.site
import models.{CarReservePostJsonResponseObj, FloorSortPostJsonRequestObj, PlaceEnum}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import utils.silhouette.MyEnv

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._


/**
  * フロア管理アクションクラス
  *
  *
  */

// フォーム定義
case class FloorDeleteForm(deleteFloorId: String)
case class FloorUpdateForm(inputPlaceId: String, inputFloorId: String, inputFloorName: String, inputExbDeviceNoListComma: String)

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
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3){
      // 選択されている現場の現場ID
      val placeId = securedRequest2User.currentPlaceId.get

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
    }else {
      Redirect(site.routes.WorkPlace.index)
    }

  }

  /** フロア更新 */
  def floorUpdate = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputFloorId" -> text
      , "inputFloorName" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.inputFloorName.empty"), {!_.isEmpty})
      , "inputExbDeviceNoListComma" -> text.verifying(Messages("error.cms.floorManage.floorUpdate.inputExbDeviceNoListComma.empty"), {!_.isEmpty})
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
      var paramDeviceList = Seq[(Int,Int)]()
      val f = form.get
      if(f.inputFloorId.isEmpty){
        // 新規フロア登録の場合 --------------------------

        // フロア名称重複チェック
        val floorList = floorDAO.selectFloorInfo(f.inputPlaceId.toInt, f.inputFloorName)
        if(floorList.nonEmpty){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputFloorName.duplicate")
        }

        // デバイスNo重複チェック
        val exbDeviceNoList = exbDAO.selectExb(f.inputPlaceId.toInt).map(exb =>{exb.exbDeviceNo})
        val inputExbDeviceNoList:Seq[String] = f.inputExbDeviceNoListComma.split("-").filter(_.isEmpty == false).toSeq

        if(inputExbDeviceNoList.exists(!_.matches("^[0-9]+$"))){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceNo.notNumeric")
        } else {
          var errDeviceNoList = inputExbDeviceNoList.filter(exbDeviceNoList contains _.toInt)
          if(errDeviceNoList.nonEmpty) {
            errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceNo.duplicate", errDeviceNoList.mkString(","))
          }else{
            // デバイスIDの取得
            inputExbDeviceNoList.foreach{e =>
              val retList = exbDAO.select(f.inputPlaceId.toInt, Option(e.toInt))
              if(retList.nonEmpty){
                paramDeviceList :+= (retList.last.exbDeviceNo, retList.last.exbDeviceId)
              }else{
                errDeviceNoList :+= e
              }
            }
            if(errDeviceNoList.nonEmpty) {
              errMsg :+= Messages("error.cms.floorManage.floorUpdate.DeviceId.notfound", errDeviceNoList.mkString(","))
            }
          }
        }

        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.FloorManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          floorDAO.insert(f.inputFloorName, f.inputPlaceId.toInt, paramDeviceList)
          // 成功で遷移
          Redirect(routes.FloorManage.index)
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
        val inputExbDeviceNoList:Seq[String] = f.inputExbDeviceNoListComma.split("-").toSeq
        if(inputExbDeviceNoList.exists(!_.matches("^[0-9]+$"))){
          errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.notNumeric")
        } else {
          var errDeviceNoList = inputExbDeviceNoList.filter(exbDeviceIdList contains _.toInt)
          if(errDeviceNoList.nonEmpty) {
            errMsg :+= Messages("error.cms.floorManage.floorUpdate.inputDeviceId.duplicate", errDeviceNoList.mkString(","))
          }else{
            // デバイスIDの取得
            inputExbDeviceNoList.foreach{e =>
              val retList = exbDAO.select(f.inputPlaceId.toInt, Option(e.toInt))
              if(retList.nonEmpty){
                paramDeviceList :+= (retList.last.exbDeviceNo, retList.last.exbDeviceId)
              }else{
                errDeviceNoList :+= e
              }
            }
            if(errDeviceNoList.nonEmpty) {
              errMsg :+= Messages("error.cms.floorManage.floorUpdate.DeviceId.notfound", errDeviceNoList.mkString(","))
            }
          }
        }

        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.FloorManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          floorDAO.updateById(securedRequest2User.currentPlaceId.get, f.inputFloorId.toInt, f.inputFloorName, paramDeviceList)
          // 成功で遷移
          Redirect(routes.FloorManage.index)
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
      // DB処理
      floorDAO.deleteById(f.deleteFloorId.toInt)
      // リダイレクト
      Redirect(routes.FloorManage.index)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.FloorManage.floorDelete"))
    }
  }

//  /** ソート */
//  def sort = SecuredAction.async(parse.json[FloorSortPostJsonRequestObj]) { implicit request =>
//    Future {
//      // リクエストボディ(JSONオブジェクト)取得
//      val o = request.body
//      if(o.floorIdComma.nonEmpty){
//        // 予約の更新
//        val floorIdStrList: Seq[String] = o.floorIdComma.split(",").toSeq
//        floorDAO.updateOrder(floorIdStrList.map{floorIdStr => floorIdStr.toInt})
//      }
//      // OKの返却
//      Ok(Json.toJson(CarReservePostJsonResponseObj(true))).as(RESPONSE_CONTENT_TYPE)
//    }
//  }

}
