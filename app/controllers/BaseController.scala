package controllers

import utils.silhouette.{AuthController, MyEnv}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import models.{FloorInfo, exCloudBtxData}

/**
  * 基底クラス
  */
trait BaseController extends AuthController {

  val CMS_LOGGED_SESSION_KEY = "cmsLogged"
  val CMS_NOT_LOGGED_RETURN_PATH = "/carSummery"

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

  /**
    * APIデータからフロアを検出する（仮設材用）
    *
    */
  implicit def getNearestFloor[A](floorInfoList: Seq[FloorInfo], apiData: exCloudBtxData): Seq[FloorInfo] = {
    val floor = floorInfoList.filter(_.exbDeviceIdList contains apiData.device_id.toString)
    if(floor.nonEmpty){
      var newFloorList = Seq[FloorInfo]()
      apiData.nearest.foreach { btx =>
        val fl:Seq[FloorInfo] = floorInfoList.filter(_.exbDeviceIdList contains btx.device_id.toString)
        newFloorList = newFloorList union fl
      }
      if(newFloorList.isEmpty){
        floor
      }else{
        var result = Seq[FloorInfo]()
        newFloorList.foreach{ f =>
          if(newFloorList.filter(_.floorId == f.floorId).length > 1){
            result :+= f
          }
        }
        if(result.nonEmpty){
          result
        }else{
          floor
        }
      }
    }else{
      floor
    }
  }

  /**
    * APIデータから指定フロアにある仮設材を検出する
    *
    */
  implicit def getItemBtxDetected[A](apiDataList: List[exCloudBtxData], floor:FloorInfo): List[exCloudBtxData] = {
    var result = List[exCloudBtxData]()

    apiDataList.foreach{apiData =>
      var flg = false
      val nearestDeviceIdList = Seq[Int](apiData.device_id) union apiData.nearest.map{n => n.device_id}

      nearestDeviceIdList.foreach{deviceId =>
        if(floor.exbDeviceIdList contains deviceId.toString){
          flg = true
        }
      }
      if(flg){
        result :+= apiData
      }
    }
    result
  }

}