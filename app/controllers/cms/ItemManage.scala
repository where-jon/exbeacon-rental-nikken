package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import controllers.site
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * その他仮設材管理アクションクラス
  *
  *
  */
// フォーム定義
case class ItemDeleteForm(
    deleteItemId: String
)
case class ItemUpdateForm(
    inputPlaceId: String
  , inputItemOtherId: String
  , inputItemOtherBtxId: String
  , inputItemOtherNo: String
  , inputItemOtherName: String
  , inputItemNote: String
  , inputItemTypeName: String
)

@Singleton
class ItemManage @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , itemOtherDAO: models.itemOtherDAO
//                           , itemDAO: models.itemDAO
//                           , btxDAO: models.btxDAO
                           , itemTypeDAO:models.ItemTypeDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 仮設材情報
//      val itemList = itemDAO.selectItemInfo(placeId)
      // その他仮設材情報
      val itemOtherList = itemOtherDAO.selectOtherMasterInfo(placeId)
      Ok(views.html.cms.itemManage(itemOtherList, placeId))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>

    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputItemOtherId" -> text
      , "inputItemOtherBtxId" -> text
      , "inputItemOtherNo" -> text
      , "inputItemOtherName" -> text
      , "inputItemNote" -> text
      , "inputItemTypeName" -> text
    )(ItemUpdateForm.apply)(ItemUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemManage.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      var errMsg = Seq[String]()
      val f = form.get

      // 種別存在チェック
      val itemTypeList = itemTypeDAO.selectItemTypeCheck(f.inputItemTypeName, f.inputPlaceId.toInt)
      if(itemTypeList.isEmpty){
        errMsg :+= Messages("error.cms.CarManage.update.NotTypeName", f.inputItemOtherNo)
      }

      if(f.inputItemOtherId.isEmpty){
        // 新規登録

//        // 名称重複チェック
//        val list = itemDAO.selectItemInfo(f.inputPlaceId.toInt, f.inputItemKindName, None)
//        if(list.nonEmpty){
//          errMsg :+= Messages("error.cms.ItemManage.update.inputItemKindName.duplicate", f.inputItemKindName)
//        }
//        // 管理No / BTXタグ
//        var itemNoList = Seq[String]()
//        var btxList = Seq[String]()
//        val lineList:Seq[String] = f.actualItemInfoStr.split("-").toSeq.filter(_.isEmpty == false).map{ line =>
//          itemNoList :+= line.split(",")(0)
//          btxList :+= line.split(",")(1)
//          line
//        }

        // 重複チェック用
//        val dbItemNoList = itemDAO.selectActualItemInfo(f.inputPlaceId.toInt)
//        val dbBtxList = btxDAO.select(super.getCurrentPlaceId)

//        // 管理Noのチェック
//        if(itemNoList.exists(!_.matches("^[0-9]+$"))){
//          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.notNumeric")
//        } else {
//          val duplicateList = itemNoList.filter(dbItemNoList.map{d => d.itemNo} contains _)
//          if(duplicateList.nonEmpty) {
//            errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.duplicate", duplicateList.mkString(","))
//          }
//        }

//        // BTXタグのチェック
//        if(btxList.exists(!_.matches("^[0-9]+$"))){
//          errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.notNumeric")
//        } else {
//          val duplicateList = btxList.filter(dbBtxList.map{d => d.btxId} contains _.toInt)
//          if(duplicateList.nonEmpty){
//            errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.duplicate", duplicateList.mkString(","))
//          }
//        }

        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.ItemManage.index).flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // 登録の実行
//          itemDAO.insert(f.inputItemKindName, f.inputNote, f.inputPlaceId.toInt, lineList, btxList.map{b=>b.toInt})

          Redirect(routes.ItemManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemManage.update"))
        }

      }else{
        // 更新の場合 --------------------------
        // 作業車・立場番号重複チェック
        // その他仮設材重複チェック用
        var dbItemNoList = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, f.inputItemOtherNo)
        dbItemNoList = dbItemNoList.filter(_.itemOtherId != f.inputItemOtherId.toInt)
                                   .filter(_.itemOtherNo == f.inputItemOtherNo)
//        if(dbItemNoList.length > 0){
//          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.duplicate", f.inputItemOtherNo)
//        }

        // 変更前タグ情報
        val preItemInfo = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, "", Option(f.inputItemOtherId.toInt)).last

        // タグチェック
        val checkList = Seq[(Int,Int)](
          (preItemInfo.itemOtherBtxId, f.inputItemOtherBtxId.toInt)
        )
        checkList.zipWithIndex.foreach { case (btxId, i) =>
          if (btxId._1 == btxId._2) {
            // 変更前と同じの場合何もしない
          } else {
            // 登録が重複する場合
            // その他仮設材TxビーコンID重複チェック
            var btxList = itemOtherDAO.selectOtherInfo(super.getCurrentPlaceId, "", None, Option(f.inputItemOtherBtxId.toInt))
            if(btxList.length > 0){
              errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.duplicate", f.inputItemOtherBtxId)
            }
          }
        }

        if(errMsg.nonEmpty){
          // エラーで遷移
          Redirect(routes.ItemManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // 更新の実行
          itemOtherDAO.update(
                              f.inputItemOtherId.toInt,
                              f.inputItemOtherNo,
                              f.inputItemOtherName,
                              f.inputItemOtherBtxId.toInt,
                              f.inputItemNote,
                              itemTypeList.last.item_type_id,
                              super.getCurrentPlaceId)
          // リダイレクト
          Redirect(routes.ItemManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemManage.update"))
        }
      }
    }
  }

  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val deleteForm = Form(mapping(
      "deleteItemId" -> text.verifying(Messages("error.cms.CarManage.delete.empty"), {
        !_.isEmpty
      })
    )(ItemDeleteForm.apply)(ItemDeleteForm.unapply))

    // フォームの取得
    val form = deleteForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.ItemManage.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get
      // DB処理
//      itemDAO.deleteById(f.deleteItemId.toInt)

      // リダイレクト
      Redirect(routes.ItemManage.index).flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemManage.delete"))
    }
  }
}
