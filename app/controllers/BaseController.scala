package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import utils.silhouette.{AuthController, MyEnv}

/**
  * 基底クラス
  */
trait BaseController extends AuthController {

  val CMS_LOGGED_SESSION_KEY = "cmsLogged"
  val CMS_NOT_LOGGED_RETURN_PATH = "/site/itemCarMaster"

  val HTML_BR = "<br/>"

  val ERROR_MSG_KEY = "errMsg"
  val SUCCESS_MSG_KEY = "successMsg"
  val KEY_PLACE_ID = "placeId"

  val RESPONSE_CONTENT_TYPE = "application/json;charset=UTF-8"

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
        "1"
      } else {
        securedRequest2User(request).placeId.get.toString
      }
    }
  }

  /**
    * セッションから現在操作中の現場IDを取得
    *
    */
  implicit def getCurrentPlaceId[A](implicit request: SecuredRequest[MyEnv, A]): Int = {
    if(securedRequest2User.currentPlaceId != None){
      securedRequest2User.currentPlaceId.get
    }else{
      1
    }
  }

  /**
    * 管理ページ認証済みかどうかを判定する
    *
    */
  implicit def isCmsLogged[A](implicit request: SecuredRequest[MyEnv, A]): Boolean = {
    if(securedRequest2User.isSysMng){
      true
    }else{
      request.session.get(CMS_LOGGED_SESSION_KEY).map { data =>
        true
      }.getOrElse {
        false
      }
    }
  }

}