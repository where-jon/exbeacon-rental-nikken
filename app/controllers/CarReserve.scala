package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models._
import org.joda.time.{DateTime, DateTimeConstants}
import org.joda.time.format.DateTimeFormat
import play.api._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.collection.immutable.List
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

import utils.silhouette._


/**
  * 高所作業車予約アクションクラス
  *
  *
  */
// フォーム
case class ReserveInputForm(
    inputCarNo: String
  , inputFloorId: String
  , inputCompanyId: String
  , reserveDate: String
)
@Singleton
class CarReserve @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , ws: WSClient
                           , carReserveDAO: models.carReserveDAO
                           , reserveDAO: models.reserveDAO
                           , floorDAO: models.floorDAO
                           , placeDAO: models.placeDAO
                           , carDAO: models.carDAO
                           , companyDAO: models.companyDAO
                               ) extends BaseController with I18nSupport {
  // レスポンスのコンテントタイプ
  val RESPONSE_CONTENT_TYPE = "application/json;charset=UTF-8"
  val DATE_FORMAT = "yyyyMMdd"
  val RESERVE_DATE_PARAM_KEY = "reserveDate"

  /** 初期表示 */
  def index = SecuredAction.async { implicit request =>

    // 入力予約日
    var reserveDateStr = new DateTime().plusDays(1).toString(DATE_FORMAT)
    if(new DateTime().getDayOfWeek() == DateTimeConstants.FRIDAY){
      reserveDateStr = new DateTime().plusDays(3).toString(DATE_FORMAT)
    }
    val reserveDateOpt = request.getQueryString(RESERVE_DATE_PARAM_KEY)
    if (reserveDateOpt.isEmpty == false) {
      reserveDateStr = reserveDateOpt.get
    }

    // 予約日オブジェクト
    val reserveDateObj = DateTime.parse(reserveDateStr, DateTimeFormat.forPattern(DATE_FORMAT))

    // 現場情報
    val placeId = super.getCurrentPlaceId
    val place = placeDAO.selectPlaceList(Seq[Int](placeId)).last

    // フロア情報
    val floorInfoList = floorDAO.selectFloorInfo(placeId)
    // 業者情報
    val companyList = companyDAO.selectCompany(placeId)
    // 作業車情報
    val carList = carDAO.selectCarInfo(placeId)
    // 予約情報
    var reserveInfoList = carReserveDAO.selectReserveForPlot(placeId, reserveDateObj.toString(DATE_FORMAT))
    // 前日の予約情報 TODO
    val beforeReserveList = reserveDAO.selectReserve(placeId, floorInfoList.map{f => f.floorId}, new DateTime().toString("yyyyMMdd"))
    // 稼働情報
    var workList = Seq[CarReserveModelPlotInfo]()

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // 稼働情報をフロア毎に生成
      floorInfoList.foreach { floor => // -- ループ start --
        // 実際の作業車Tx
        val carsAtFloor = list
          .filter(floor.exbDeviceIdList contains _.device_id.toString) // フロアのEXBデバイスIDに合致するもの
          .filter(carList.map{c => c.carBtxId} contains _.btx_id)  // 予約作業車のBTXidに合致するもの

        var carExistCountAtFloor = 0
        carsAtFloor.foreach{carBtx =>
            val rest = carList.filter(_.carBtxId == carBtx.btx_id)
            if(rest.isEmpty == false) { // -- if start --
              val c = rest.last

              // リストにデータを詰める
              carExistCountAtFloor += 1
              workList :+= CarReserveModelPlotInfo( //--- 設定
                                floor.floorId.toString
                              , companyIdStr = {
                                  val ddd = beforeReserveList.filter(_.floorId == floor.floorId).filter(_.carId == c.carId)
                                  if (ddd.isEmpty == false) {
                                    ddd.last.companyId.toString
                                  } else {
                                    ""
                                  }
                                }
                              , c.carId.toString
                              , c.carNo
                          ) // --設定
            } // -- if end --
          }
      } // -- ループ end --
      reserveInfoList = reserveInfoList.map { r =>
        val rest = workList.filter(_.carIdStr == r.carIdStr)
        if(rest.isEmpty == false){
          val dataBefore = s"""${rest.last.floorIdStr}/${rest.last.companyIdStr}"""
          CarReserveModelPlotInfo(r.floorIdStr, r.companyIdStr, r.carIdStr, r.carNo, r.reserveIdStr, dataBefore)
        }else{
          r
        }
      }
      Ok(views.html.carReserve(companyList, floorInfoList, reserveInfoList, workList, reserveDateObj))
    }
  }

  /** 削除 */
  def delete = SecuredAction.async(parse.json[CarReservePostJsonRequestObj]) { implicit request =>
    Future {
      // リクエストボディ(JSONオブジェクト)取得
      val o = request.body
      // 予約の削除
      reserveDAO.delete(o.reserveId)
      // OKの返却
      Ok(Json.toJson(CarReservePostJsonResponseObj(true))).as(RESPONSE_CONTENT_TYPE)
    }
  }

  /** 更新 */
  def update = SecuredAction.async(parse.json[CarReservePostJsonRequestObj]) { implicit request =>
    Future {
      // リクエストボディ(JSONオブジェクト)取得
      val o = request.body
      // 予約の更新
      reserveDAO.update(o.reserveId, o.floorId, o.companyId, o.reserveDate)
      // OKの返却
      Ok(Json.toJson(CarReservePostJsonResponseObj(true))).as(RESPONSE_CONTENT_TYPE)
    }
  }

  /** 新規登録 */
  def register = SecuredAction.async(parse.json[CarReservePostJsonRequestObj]) { implicit request =>
    Future {
      // リクエストボディ(JSONオブジェクト)取得
      val o = request.body
      // 予約の更新
      val id = reserveDAO.insert(o.carId, o.floorId, o.companyId, o.reserveDate)
      // OKの返却
      Ok(Json.toJson(CarReservePostJsonResponseObj(true, id))).as(RESPONSE_CONTENT_TYPE)
    }
  }

  /** 新規登録（モーダル画面からの登録） */
  def registerModal = SecuredAction { implicit request =>

    // フォームの準備
    val inputForm = Form(mapping(
        "inputCarNo" -> text.verifying(Messages("error.CarReserve.registerModal.inputCarNo.format"), {_.matches("^[0-9]+$")})
      , "inputFloorId" -> text
      , "inputCompanyId" -> text
      , "reserveDate" -> text
    )(ReserveInputForm.apply)(ReserveInputForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors){
      // エラーでリダイレクト遷移
      val reserveDate: String = form.data("reserveDate")
      Redirect(s"""${routes.CarReserve.index().path}?${RESERVE_DATE_PARAM_KEY}=${reserveDate}""")
        .flashing(ERROR_MSG_KEY -> form.errors.map(_.message).mkString(HTML_BR))
    }else{
      var errMsg = Seq[String]()
      val f = form.get

      // 作業車存在チェック
      val carList = carDAO.selectCarInfo(super.getCurrentPlaceId, f.inputCarNo, None)
      if(carList.isEmpty){
        // なければエラー
        errMsg :+=  Messages("error.CarReserve.registerModal.inputCarNo.exist")
      }
      if(errMsg.isEmpty){
        // 同じ箇所にすでに同じ車がある場合もエラー
        val duplicateReserveList = reserveDAO.selectReserve(
                                                            super.getCurrentPlaceId
                                                            , Seq[Int](f.inputFloorId.toInt)
                                                            , f.reserveDate
                                                            , Seq[Int](f.inputCompanyId.toInt)
                                                            , Seq[Int](carList.last.carId)
                                                          )
        if(duplicateReserveList.isEmpty == false){
          errMsg :+=  Messages("error.CarReserve.registerModal.inputCarNo.duplicate")
        }
      }

      if(errMsg.isEmpty == false){
        // エラーで遷移
        Redirect(s"""${routes.CarReserve.index().path}?${RESERVE_DATE_PARAM_KEY}=${f.reserveDate}""")
          .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      }else{
        // DB登録実行
        reserveDAO.insert(carList.last.carId, f.inputFloorId.toInt, f.inputCompanyId.toInt, f.reserveDate)
        // 成功で遷移
        Redirect(s"""${routes.CarReserve.index().path}?${RESERVE_DATE_PARAM_KEY}=${f.reserveDate}""")
          .flashing(SUCCESS_MSG_KEY -> Messages("success.CarReserve.registerModal"))
      }
    }
  }
}
