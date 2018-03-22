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
  * 作業車管理アクションクラス
  *
  *
  */

// フォームの定義
case class CarUpdateForm(
     inputPlaceId: String
   , inputCarId: String
   , inputCarNo: String
   , inputCarName: String
   , inputCarBtxId: String
   , inputCarKeyBtxId: String
)

case class CarDeleteForm(
  inputCarId: String
)

@Singleton
class CarManage @Inject()(config: Configuration
                          , val silhouette: Silhouette[MyEnv]
                          , val messagesApi: MessagesApi
                          , carDAO: models.carDAO
                          , btxDAO: models.btxDAO
                               ) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    // 選択された現場の現場ID
    val placeIdStr = super.getCurrentPlaceIdStr

    // 業者情報
    val carList = carDAO.selectCarInfo(placeId = placeIdStr.toInt)

    Ok(views.html.cms.carManage(carList, placeIdStr.toInt))
  }


  /** 登録・更新 */
  def update = SecuredAction { implicit request =>
    // フォームの準備
    val inputForm = Form(mapping(
        "inputPlaceId" -> text
      , "inputCarId" -> text
      , "inputCarNo" -> text.verifying(Messages("error.cms.CarManage.update.inputCarNo.empty"), {_.matches("^[0-9]+$")})
      , "inputCarName" -> text
      , "inputCarBtxId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarBtxId.empty"), {_.matches("^[0-9]+$")})
      , "inputCarKeyBtxId" -> text.verifying(Messages("error.cms.CarManage.update.inputCarKeyBtxId.empty"), {_.matches("^[0-9]+$")})
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

      if(f.inputCarId.isEmpty){
        // 新規登録の場合 --------------------------
        // 作業車番号重複チェック
        val carList = carDAO.selectCarInfo(super.getCurrentPlaceIdStr.toInt, f.inputCarNo)
        if(carList.length > 0){
          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
        }
        // TxタグNo重複チェック
        val btxList = btxDAO.select(super.getCurrentPlaceIdStr.toInt)
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
          carDAO.insert(f.inputCarNo, f.inputCarName, f.inputCarBtxId.toInt, f.inputCarKeyBtxId.toInt, f.inputPlaceId.toInt)

          Redirect(routes.CarManage.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.update"))
        }

      }else{
        // 更新の場合 --------------------------
        // 作業車番号重複チェック
        var carList = carDAO.selectCarInfo(super.getCurrentPlaceIdStr.toInt, f.inputCarNo)
        carList = carList.filter(_.carId != f.inputCarId.toInt).filter(_.carNo == f.inputCarNo)
        if(carList.length > 0){
          errMsg :+= Messages("error.cms.CarManage.update.inputCarNo.duplicate", f.inputCarNo)
        }

        // 変更前タグ情報
        val preCarInfo = carDAO.selectCarInfo(super.getCurrentPlaceIdStr.toInt, "", Option(f.inputCarId.toInt)).last

        // タグチェック
        val checkList = Seq[(Int,Int)](
            (preCarInfo.carBtxId, f.inputCarBtxId.toInt)
          , (preCarInfo.carKeyBtxId, f.inputCarKeyBtxId.toInt)
        )
        checkList.zipWithIndex.foreach{case (btxId, i) =>
          if(btxId._1 == btxId._2){
            // OK
          }else{
            val btxList = btxDAO.select(super.getCurrentPlaceIdStr.toInt, Seq[Int](btxId._2))
            if(btxList.isEmpty == false){
              // NG
              if(i == 0){
                errMsg :+= Messages("error.cms.CarManage.update.inputCarBtxId.duplicate", btxId._2)
              }else{
                errMsg :+= Messages("error.cms.CarManage.update.inputCarKeyBtxId.duplicate", btxId._2)
              }
            }else{
              // OK
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
            , super.getCurrentPlaceIdStr.toInt
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
      "inputCarId" -> text
    )(CarDeleteForm.apply)(CarDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    val f = form.get

    // DB処理

    // 検索
    val carList = carDAO.selectCarInfo(super.getCurrentPlaceIdStr.toInt, "", Option(f.inputCarId.toInt))
    // タグ
    var txTagList = Seq[Int]()
    if(carList.length > 0){
      // タグ情報収集
      carList.map{ car =>
        txTagList :+= car.carBtxId
        txTagList :+= car.carKeyBtxId
      }

      // 削除
      carDAO.delete(f.inputCarId.toInt, super.getCurrentPlaceIdStr.toInt, txTagList)
    }

    // リダイレクト
    Redirect(routes.CarManage.index)
      .flashing(SUCCESS_MSG_KEY -> Messages("success.cms.CarManage.delete"))
  }

}
