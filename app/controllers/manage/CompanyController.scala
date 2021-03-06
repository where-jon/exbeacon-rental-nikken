package controllers.manage

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import javax.inject.{Inject, Singleton}
import models.manage.{CompanyDeleteForm, CompanyUpdateForm}
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.silhouette.MyEnv


/**
  * 業者管理アクションクラス
  *
  *
  */

@Singleton
class CompanyController @Inject()(
 config: Configuration,
 val silhouette: Silhouette[MyEnv],
 val messagesApi: MessagesApi,
 companyService: services.manage.CompanyService
) extends BaseController with I18nSupport {


  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if (reqIdentity.level >= 2) {
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
      val companyList = companyService.selectCompanyByPlaceId(placeId)
      Ok(views.html.manage.company(companyList, placeId))
    } else {
      Redirect(site.routes.ItemCarListController.index)
    }
  }


  /** 更新 */
  def update = SecuredAction { implicit request =>
    // 権限レベルを取得
    val reqIdentity = request.identity
    // フォームの準備
    val inputForm = Form(mapping(
      "inputPlaceId" -> text
      , "inputCompanyId" -> text
      , "inputCompanyName" -> text.verifying(Messages("error.manage.company.update.inputCompanyName.empty"), {
        !_.isEmpty
      })
      , "inputNote" -> text
    )(CompanyUpdateForm.apply)(CompanyUpdateForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.CompanyController.index()).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      var errMsg = Seq[String]()
      val f = form.get
      if (f.inputCompanyId.isEmpty) {
        // 新規登録の場合 --------------------------
        // 名称重複チェック
        val companyList = companyService.selectCompanyByPlaceId(super.getCurrentPlaceId, f.inputCompanyName)
        if (companyList.nonEmpty) {
          errMsg :+= Messages("error.manage.company.update.inputCompanyName.duplicate", f.inputCompanyName)
        }
        if (errMsg.nonEmpty) {
          // エラーで遷移
          Redirect(routes.CompanyController.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        } else {
          // DB処理
          companyService.insertCompany(f.inputCompanyName, f.inputNote, f.inputPlaceId.toInt)

          Redirect(routes.CompanyController.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.company.update"))
        }

      } else {
        // 更新の場合 --------------------------
        // 名称重複チェック
        var companyList = companyService.selectCompanyByPlaceId(super.getCurrentPlaceId)
        companyList = companyList.filter(_.companyId != f.inputCompanyId.toInt).filter(_.companyName == f.inputCompanyName)
        if (companyList.nonEmpty) {
          errMsg :+= Messages("error.manage.company.update.inputCompanyName.duplicate", f.inputCompanyName)
        }

        // 予約情報テーブルに作業車・立馬IDが存在していないか
        val itemReserveList = companyService.selectCarReserveCheck(super.getCurrentPlaceId, f.inputCompanyId.toInt)
        if (itemReserveList.length > 0) {
          var chkFlg: Int = 0
          var companyNm: String = ""
          // 現在時刻設定
          val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
          val currentTime = new Date();
          val mTime = mSimpleDateFormat.format(currentTime)
          val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
          for (itemReserve <- itemReserveList) {
            companyNm = itemReserve.companyName
            if (itemReserve.reserveEndDate.toInt < mTime.toInt) {
              // 予約期間が前日の場合、または期間が終日の場合
              if (chkFlg != 1) {
                chkFlg = 2
              }

            } else {
              // 当日以降
              if (itemReserve.workTypeId == 1) {
                // 午前予約の場合
                if (itemReserve.reserveEndDate.toInt == mTime.toInt) {
                  if (mHour < 13) {
                    // 予約済み（使用中）
                    chkFlg = 1
                  } else {
                    // 現在時刻が13時を過ぎていた場合
                    if (chkFlg != 1) {
                      chkFlg = 2
                    }
                  }
                } else {
                  // 予約済み（使用中）
                  chkFlg = 1
                }
              } else if (itemReserve.workTypeId == 2) {
                // 午後予約の場合
                if (itemReserve.reserveEndDate.toInt == mTime.toInt) {
                  if (mHour < 17) {
                    // 予約済み（使用中）
                    chkFlg = 1
                  } else {
                    // 現在時刻が17時を過ぎていた場合
                    if (chkFlg != 1) {
                      chkFlg = 2
                    }
                  }
                } else {
                  // 予約済み（使用中）
                  chkFlg = 1
                }
              } else {
                // 未来で予約済み
                chkFlg = 1
              }
            }
          }
          if (chkFlg == 1) {
            errMsg :+= Messages("error.manage.company.update.inputCompanyName.use.noChange", companyNm);
          } else if (chkFlg == 2) {
            if (reqIdentity.level < 3) {
              // 権限がレベル３以下のみ予約情報が有る場合エラーにする
              errMsg :+= Messages("error.manage.company.update.inputCompanyName.use.exceed", companyNm);
            }
          }
        }

        if (errMsg.nonEmpty) {
          // エラーで遷移
          Redirect(routes.CompanyController.index)
            .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
        } else {
          // DB処理
          companyService.updateCompanyById(f.inputCompanyId.toInt, f.inputCompanyName, f.inputNote)
          Redirect(routes.CompanyController.index)
            .flashing(SUCCESS_MSG_KEY -> Messages("success.manage.company.update"))
        }
      }
    }
  }


  /** 削除 */
  def delete = SecuredAction { implicit request =>
    // 権限レベルを取得
    val reqIdentity = request.identity
    var errMsg = Seq[String]()
    // フォームの準備
    val inputForm = Form(mapping(
      "deleteCompanyId" -> text.verifying(Messages("error.manage.itemCar.delete.empty"), {
        !_.isEmpty
      })
    )(CompanyDeleteForm.apply)(CompanyDeleteForm.unapply))

    // フォームの取得
    val form = inputForm.bindFromRequest
    if (form.hasErrors) {
      // エラーメッセージ
      val errMsg = form.errors.map(_.message).mkString(HTML_BR)
      // リダイレクトで画面遷移
      Redirect(routes.CompanyController.index).flashing(ERROR_MSG_KEY -> errMsg)
    } else {
      val f = form.get
      // 予約情報テーブルに作業車・立馬IDが存在していないか
      val itemReserveList = companyService.selectCarReserveCheck(super.getCurrentPlaceId, f.deleteCompanyId.toInt)
      if (itemReserveList.length > 0) {
        var chkFlg: Int = 0
        var companyNm: String = ""
        // 現在時刻設定
        val mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN)
        val currentTime = new Date();
        val mTime = mSimpleDateFormat.format(currentTime)
        val mHour = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()).toInt
        for (itemReserve <- itemReserveList) {
          companyNm = itemReserve.companyName
          if (itemReserve.reserveEndDate.toInt < mTime.toInt) {
            // 予約期間が前日の場合、または期間が終日の場合
            if (chkFlg != 1) {
              chkFlg = 2
            }

          } else {
            // 当日以降
            if (itemReserve.workTypeId == 1) {
              // 午前予約の場合
              if (itemReserve.reserveEndDate.toInt == mTime.toInt) {
                if (mHour < 13) {
                  // 予約済み（使用中）
                  chkFlg = 1
                } else {
                  // 現在時刻が13時を過ぎていた場合
                  if (chkFlg != 1) {
                    chkFlg = 2
                  }
                }
              } else {
                // 予約済み（使用中）
                chkFlg = 1
              }
            } else if (itemReserve.workTypeId == 2) {
              // 午後予約の場合
              if (itemReserve.reserveEndDate.toInt == mTime.toInt) {
                if (mHour < 17) {
                  // 予約済み（使用中）
                  chkFlg = 1
                } else {
                  // 現在時刻が17時を過ぎていた場合
                  if (chkFlg != 1) {
                    chkFlg = 2
                  }
                }
              } else {
                // 予約済み（使用中）
                chkFlg = 1
              }
            } else {
              // 未来で予約済み
              chkFlg = 1
            }
          }
        }
        if (chkFlg == 1) {
          errMsg :+= Messages("error.manage.company.delete.inputCompanyName.use.noChange", companyNm);
        } else if (chkFlg == 2) {
          if (reqIdentity.level < 3) {
            // 権限がレベル３以下のみ予約情報が有る場合エラーにする
            errMsg :+= Messages("error.manage.company.delete.inputCompanyName.use.exceed", companyNm);
          }
        }
      }
      if (errMsg.nonEmpty) {
        // エラーで遷移
        Redirect(routes.CompanyController.index)
          .flashing(ERROR_MSG_KEY -> errMsg.mkString(HTML_BR))
      } else {
        // DB処理
        companyService.deleteCompanyById(f.deleteCompanyId.toInt)

        // リダイレクト
        Redirect(routes.CompanyController.index).flashing(SUCCESS_MSG_KEY -> Messages("success.manage.company.delete"))
      }
    }
  }

}
