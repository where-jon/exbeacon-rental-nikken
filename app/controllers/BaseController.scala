package controllers

import java.math.BigDecimal

import utils.silhouette.{AuthController, MyEnv}
import play.api.mvc.{Request, Session}
import com.mohiva.play.silhouette.api.actions
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import models.User

/**
  * 基底クラス
  */
trait BaseController extends AuthController {

  val HTML_BR = "<br/>"

  val ERROR_MSG_KEY = "errMsg"
  val SUCCESS_MSG_KEY = "successMsg"

  val KEY_PLACE_ID = "placeId"
  val CURRENT_PLACE_ID = "currentPlaceId"

  /**
    * 選択現場ID取得メソッド
    *
    */
  implicit def getRequestPlaceIdStr[A](implicit request: SecuredRequest[MyEnv, A]): String = {
    val reqPlaceId = request.getQueryString(KEY_PLACE_ID)
    if (reqPlaceId.isEmpty == false) {
      reqPlaceId.get
    } else {
      if (securedRequest2User(request).placeId == None) {
        ""
      } else {
        securedRequest2User(request).placeId.get.toString
      }
    }
  }

  /**
    * セッションから現在操作中の現場IDを取得
    *
    */
  implicit def getCurrentPlaceIdStr[A](implicit request: SecuredRequest[MyEnv, A]): String = {
    request.session.get(CURRENT_PLACE_ID).map { placeId =>
      placeId
    }.getOrElse {
      securedRequest2User(request).placeId.toString
    }
  }
}