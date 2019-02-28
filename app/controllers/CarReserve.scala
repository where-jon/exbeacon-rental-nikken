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
                           , floorDAO: models.system.floorDAO
                           , placeDAO: models.placeDAO
                           , carDAO: models.manage.itemCarDAO
                           , companyDAO: models.manage.companyDAO
                           , btxLastPositionDAO: models.btxLastPositionDAO
                               ) extends BaseController with I18nSupport {
  // レスポンスのコンテントタイプ

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
    // 指定日の予約情報
    var reserveInfoList = carReserveDAO.selectReserveForPlot(placeId, reserveDateObj.toString(DATE_FORMAT))
    // 今日の予約情報（稼働用）
    val beforeReserveList = reserveDAO.selectReserve(placeId, floorInfoList.map{f => f.floorId}, new DateTime().toString(DATE_FORMAT))
    // 稼働情報
    var workList = Seq[CarReserveModelPlotInfo]()
    // 履歴input
    var inputPosition = Seq[BtxLastPosition]()

    // API呼び出し
    var url = place.btxApiUrl
    if(url.isEmpty){
      url = config.getString("excloud.dummy.url").get
    }
    ws.url(url).get().map { response =>
      // APIデータ
      val list = Json.parse(response.body).asOpt[List[exCloudBtxData]].getOrElse(Nil)

      // 稼働情報生成
      list.foreach { apiData =>
        var floorIdStr: String = ""
        var companyIdStr: String = ""
        var carIdStr: String = ""
        var carNo: String = ""
        //var reserveIdStr: String = ""
        //var dataBefore: String = ""

        val car = carList.filter(_.itemCarBtxId == apiData.btx_id)
        if(car.nonEmpty){
          // ID, 作業車番号 --
          carIdStr = car.last.itemCarId.toString
          carNo = car.last.itemCarNo
          // フロア --
          val floor = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
          if(floor.nonEmpty){
            floorIdStr = floor.last.floorId.toString//
          }else{
            // 履歴DBから取得
            val hist = btxLastPositionDAO.find(placeId, Seq[Int](car.last.itemCarBtxId))
            if(hist.nonEmpty){
              floorIdStr = hist.last.floorId.toString()
            }else{
              // 表示なし
              Logger.warn(s"履歴が無いため表示なし。現場ID = ${placeId}, 作業車番号 = ${carNo}, btx_id = ${apiData.btx_id}")
            }
          }
          // 業者 --
          val beforeReserve = beforeReserveList.filter(_.carId == car.last.itemCarId)
          if(beforeReserve.nonEmpty){
            // 予約あり
            companyIdStr = beforeReserve(0).companyId.toString()
          }else{
            // 前日予約無
          }

          workList :+= CarReserveModelPlotInfo(floorIdStr, companyIdStr, carIdStr, carNo)
        }else{
          // 作業車DB未登録
        }

        // 履歴のインプットを貯める
        val floors = utils.BtxUtil.getNearestFloor(floorInfoList, apiData)
        if(floors.nonEmpty){
          inputPosition :+= BtxLastPosition(apiData.btx_id, placeId, floors.last.floorId)
        }
      }//-- loop end

      // 履歴の登録
      btxLastPositionDAO.update(inputPosition)

      // 予約情報生成
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
                                                            , Seq[Int](carList.last.itemCarId)
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
        reserveDAO.insert(carList.last.itemCarId, f.inputFloorId.toInt, f.inputCompanyId.toInt, f.reserveDate)
        // 成功で遷移
        Redirect(s"""${routes.CarReserve.index().path}?${RESERVE_DATE_PARAM_KEY}=${f.reserveDate}""")
          .flashing(SUCCESS_MSG_KEY -> Messages("success.CarReserve.registerModal"))
      }
    }
  }
}
