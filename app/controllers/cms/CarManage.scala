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
  * 作業車・立場管理アクションクラス
  *
  *
  */

// フォームの定義
case class CarUpdateForm(
     inputPlaceId: String
   , inputCarId: String
   , inputCarNo: String
   , inputCarBtxId: String
   , inputCarKeyBtxId: String
   , inputCarTypeName: String
   , inputCarName: String
)

case class CarDeleteForm(
    deleteCarId: String
)

@Singleton
class CarManage @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , carDAO: models.itemCarDAO
                          , exbDAO: models.ExbDAO
                          , itemTypeDAO:models.ItemTypeDAO
                          , btxDAO: models.btxDAO
                               ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 2){
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
//      val carList = carDAO.selectCarInfo(placeId = placeId)
      val carList = carDAO.selectCarMasterInfo(placeId = placeId)

      Ok(views.html.cms.carManage(carList, placeId))
    }else {
      Redirect(site.routes.WorkPlace.index)
    }
  }

  /** 登録・更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputCarId" -> text
      , "inputCarNo" -> text.verifying(Messages("error.cms.CarManage.update.inputCarNo.empty"), {_.matches("^[0-9]+$")})
      , "inputCarBtxId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarBtxId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarKeyBtxId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarKeyBtxId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarTypeName" -> text
      , "inputCarName" -> text
    )(CarUpdateForm.apply)(CarUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      Redirect(routes.CarManage.index()).flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{

      var errMsg = Seq[String]()
      val f = form.get

      if(f.inputCarBtxId == f.inputCarKeyBtxId){
        errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.inputCarKeyBtxId.duplicate", f.inputCarNo)
      }

//      // 種別存在チェック
//      val itemTypeList = itemTypeDAO.selectItemTypeCheck(f.inputCarTypeName, f.inputPlaceId.toInt)
//      if(itemTypeList.isEmpty){
//        errMsg :+= Messages("error.cms.CarManage.update.NotTypeName", f.inputCarNo)
//      }

      if(f.inputCarId.isEmpty){
        // 新規登録の場合 --------------------------
        // 作業車番号重複チェック
        val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, f.inputCarNo)
        if(carList.length > 0){
          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
        }
        // TxタグNo重複チェック
        val btxList = btxDAO.select(super.getCurrentPlaceId)
        if(btxList.filter(_.btxId == f.inputCarBtxId.toInt).isEmpty == false){
          errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.duplicate", f.inputCarBtxId)
        }
        if(btxList.filter(_.btxId == f.inputCarKeyBtxId.toInt).isEmpty == false){
          errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.duplicate", f.inputCarKeyBtxId)
        }

        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.CarManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          carDAO.insert(f.inputCarNo, f.inputCarName, f.inputCarBtxId.toInt, f.inputCarKeyBtxId.toInt, f.inputPlaceId.toInt,1)

          Redirect(routes.CarManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.update"))
        }

      }else{
        // 更新の場合 --------------------------
        // 作業車番号重複チェック
        var carList = carDAO.selectCarInfo(super.getCurrentPlaceId, f.inputCarNo)
//        carList = carList.filter(_.itemCarId != f.inputCarId.toInt).filter(_.itemCarNo == f.inputCarNo)
//        if(carList.length > 0){
//          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
//        }

        // 変更前タグ情報
        val preCarInfo = carDAO.selectCarInfo(super.getCurrentPlaceId, "", Option(f.inputCarId.toInt)).last

        if(f.inputCarKeyBtxId.toInt == preCarInfo.itemCarBtxId && f.inputCarBtxId.toInt == preCarInfo.itemCarKeyBtxId) {
          // 入力を逆転している場合は何もしない
        } else {
          // タグチェック
          val checkList = Seq[(Int,Int)](
            (preCarInfo.itemCarBtxId, f.inputCarBtxId.toInt)
            , (preCarInfo.itemCarKeyBtxId, f.inputCarKeyBtxId.toInt)
          )
          checkList.zipWithIndex.foreach { case (btxId, i) =>
            if (btxId._1 == btxId._2) {
              // 変更前と同じの場合何もしない
            } else {
              val btxList = btxDAO.select(super.getCurrentPlaceId, Seq[Int](btxId._2))
              // 対象現場に既存登録がある場合
              if (!btxList.isEmpty) {
                  // 登録が重複する場合
                if (i == 0) {
                  if (f.inputCarBtxId.toInt == preCarInfo.itemCarKeyBtxId) {
                      // 作業車Txに変更前の鍵Txを登録する、且つ鍵Txが変更されている場合は何もしない
                  } else {
                    errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.duplicate", btxId._2)
                  }
                } else {
                  if (f.inputCarKeyBtxId.toInt == preCarInfo.itemCarBtxId) {
                      // 鍵Txに変更前の作業車Txを登録する場合は何もしない
                  } else {
                    errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.duplicate", btxId._2)
                  }
                }
              }
            }
          }
        }

        if(errMsg.isEmpty == false){
          // エラーで遷移
          Redirect(routes.CarManage.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        }else{
          // DB処理
          carDAO.update(
              f.inputCarId.toInt
            , f.inputCarNo
            , f.inputCarName
            , f.inputCarBtxId.toInt
            , f.inputCarKeyBtxId.toInt
            , super.getCurrentPlaceId
            , preCarInfo.itemCarBtxId
            , preCarInfo.itemCarKeyBtxId
          )

          Redirect(routes.CarManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.update"))
        }
      }
    }
  }




  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
      "deleteCarId" -> text.verifying(Messages("error.cms.CarManage.delete.empty"), {
        !_.isEmpty
      })
    )(CarDeleteForm.apply)(CarDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.CarManage.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get

      // 検索
      val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, "", Option(f.deleteCarId.toInt))
      // タグ
      var txTagList = Seq[Int]()
      if (carList.length > 0) {
        // タグ情報収集
        carList.map { car =>
          txTagList :+= car.itemCarBtxId
          txTagList :+= car.itemCarKeyBtxId
        }

        // 削除
        carDAO.delete(f.deleteCarId.toInt, super.getCurrentPlaceId, txTagList)
      }

      // リダイレクト
      Redirect(routes.CarManage.index)
        .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.delete"))
    }
  }
}
