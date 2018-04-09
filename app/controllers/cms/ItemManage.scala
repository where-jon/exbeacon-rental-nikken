package controllers.cms

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 仮設材管理アクションクラス
  *
  *
  */
// フォーム定義
case class ItemDeleteForm(inputPlaceId: String, inputItemKindId: String)
case class ItemUpdateForm(inputPlaceId: String, inputItemKindId: String, inputItemKindName: String, inputNote: String, actualItemInfoStr: String)

@Singleton
class ItemManage @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , itemDAO: models.itemDAO
                           , btxDAO: models.btxDAO
                               ) extends BaseController with I18nSupport {

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 選択された現場の現場ID
    val placeId = super.getCurrentPlaceId
    // 仮設材情報
    val itemList = itemDAO.selectItemInfo(placeId)

    Ok(views.html.cms.itemManage(itemList, placeId))
  }

  /** 更新 */
  def update = SecuredAction { implicit request =>

    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      , "inputItemKindId" -> text
      , "inputItemKindName" -> text.verifying(Messages("error.cms.ItemManage.update.inputItemKindName.empty"), {!_.isEmpty})
      , "inputNote" -> text
      , "actualItemInfoStr" -> text.verifying(Messages("error.cms.ItemManage.update.actualItemInfoStr.empty"), {!_.isEmpty})
    )(ItemUpdateForm.apply)(ItemUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.ItemManage.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      var errMsg = Seq[String]()
      val f = form.get

      if(f.inputItemKindId.isEmpty){
        // 新規登録

        // 名称重複チェック
        val list = itemDAO.selectItemInfo(f.inputPlaceId.toInt, f.inputItemKindName, None)
        if(list.length > 0){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemKindName.duplicate", f.inputItemKindName)
        }
        // 管理No / BTXタグ
        var itemNoList = Seq[String]()
        var btxList = Seq[String]()
        val lineList:Seq[String] = f.actualItemInfoStr.split("-").toSeq.filter(_.isEmpty == false).map{ line =>
          itemNoList :+= line.split(",")(0)
          btxList :+= line.split(",")(1)
          line
        }

        // 重複チェック用
        val dbItemNoList = itemDAO.selectActualItemInfo(f.inputPlaceId.toInt)
        val dbBtxList = btxDAO.select(super.getCurrentPlaceId)

        // 管理Noのチェック
        if(itemNoList.filter(_.matches("^[0-9]+$")).isEmpty){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.notNumeric")
        }
        if(itemNoList.filter(dbItemNoList.map{d => d.itemNo} contains _).isEmpty == false){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.duplicate")
        }
        // BTXタグのチェック
        if(btxList.filter(_.matches("^[0-9]+$")).isEmpty){
          errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.notNumeric")
        }
        if(btxList.filter(dbBtxList.map{d => d.btxId} contains _.toInt).isEmpty == false){
          errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.duplicate")
        }

        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.ItemManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // 登録の実行
          itemDAO.insert(f.inputItemKindName, f.inputNote, f.inputPlaceId.toInt, lineList, btxList.map{b=>b.toInt})

          Redirect(routes.ItemManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemManage.update"))
        }

      }else{
        // 更新

        // 名称重複チェック
        var list = itemDAO.selectItemInfo(f.inputPlaceId.toInt, f.inputItemKindName, None)
        list = list.filter(_.itemKindId != f.inputItemKindId.toInt).filter(_.itemKindName == f.inputItemKindName)
        if(list.length > 0){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemKindName.duplicate", f.inputItemKindName)
        }

        // 管理No / BTXタグ
        var itemNoList = Seq[String]()
        var btxList = Seq[String]()
        val lineList:Seq[String] = f.actualItemInfoStr.split("-").toSeq.filter(_.isEmpty == false).map{ line =>
          itemNoList :+= line.split(",")(0)
          btxList :+= line.split(",")(1)
          line
        }

        // 重複チェック用
        val dbItemNoList = itemDAO.selectActualItemInfo(placeId =f.inputPlaceId.toInt, excludeItemKindId = Option(f.inputItemKindId.toInt))
        val originalBtxList = itemDAO.selectActualItemInfo(placeId = f.inputPlaceId.toInt, includeItemKindId = Option(f.inputItemKindId.toInt))
        var dbBtxList :Seq[models.Btx] = btxDAO.select(f.inputPlaceId.toInt)
        dbBtxList = dbBtxList.filter(d=>{
          (originalBtxList.map{x=>x.itemBtxId}.contains(d.btxId) == false)
        })

        // 管理Noのチェック
        if(itemNoList.filter(_.matches("^[0-9]+$")).isEmpty){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.notNumeric")
        }
        if(itemNoList.filter(dbItemNoList.map{d => d.itemNo} contains _).isEmpty == false){
          errMsg :+= Messages("error.cms.ItemManage.update.inputItemNo.duplicate")
        }
        // BTXタグのチェック
        if(btxList.filter(_.matches("^[0-9]+$")).isEmpty){
          errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.notNumeric")
        }
        if(btxList.filter(dbBtxList.map{d => d.btxId} contains _.toInt).isEmpty == false){
          errMsg :+= Messages("error.cms.ItemManage.update.inputBtxId.duplicate")
        }

        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.ItemManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // 更新の実行
          itemDAO.update(f.inputItemKindId.toInt, f.inputItemKindName, f.inputNote, f.inputPlaceId.toInt, lineList, btxList.map{b=>b.toInt})

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
    val inputForm = Form(mapping(
      "deleteItemId" -> text
      , "deleteItemId" -> text
    )(ItemDeleteForm.apply)(ItemDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    val f = form.get
    // DB処理
    itemDAO.deleteById(f.inputItemKindId.toInt)

    // リダイレクト
    Redirect(routes.ItemManage.index)
      .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.ItemManage.delete"))
  }
}
