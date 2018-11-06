package actors

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.Actor
import javax.inject.Inject
import models._
import models.api.Mail
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import play.libs.mailer.MailerClient
import java.util.regex.Pattern

case class MailInfo(
    magType: String
  , fromUser: String
  , userEmail: String
  , placeName: List[String]
)

/**
  * メールを送信バッチ
  *
  */
class NoticeMailActor @Inject()(config: Configuration
                                , ws: WSClient
                                , mailerClient: MailerClient
                                , val messagesApi: MessagesApi
                                , userDAO: UserDAO
                                , itemlogDAO: ItemLogDAO
                                , reserveMasterDAO: ReserveMasterDAO
                                , placeDAO: placeDAO
  ) extends Actor with I18nSupport {
  val BATCH_NAME = "メール送信"
  val WORKING_STATUS = 1  // 施行中
  val MAGTYPE_DEVELOP = "develop"  // 開発者
  val MAGTYPE_SITE_MANAGER = "SiteManager"  // 現場責任者
  private val enableLogging = config.getBoolean("akka.quartz.schedules.NoticeMailActor.noticeMailStart").getOrElse(false)
  private val noticeDaysAgo = config.getInt("akka.quartz.schedules.NoticeMailActor.noticeDaysAgo").getOrElse(0)
  private val developMailAddress = config.getString("akka.quartz.schedules.NoticeMailActor.developMailAddress").getOrElse(0)
  private val sendMaileFromUser = config.getString("play.mailer.from").getOrElse(0)
  private val deleteInterval = config.getInt("akka.quartz.schedules.ItemLogDataDeleteActor.logDeleteInterval").getOrElse(0)
  private val deletionExclusionSite = config.getString("akka.quartz.schedules.ItemLogDataDeleteActor.deletionExclusionSite").getOrElse(0)
  // テスト用
  private val noticeMailTestDate = config.getString("akka.quartz.schedules.NoticeMailActor.noticeMailTestDate").getOrElse(0)

  def receive = {
    case msg:String => {
      execute()
    }
  }

  /**
    * 実際の処理
    */
  private def execute():Unit = {
    if ((enableLogging && noticeDaysAgo != 0) &&
        (noticeDaysAgo >= 4 && noticeDaysAgo <= 20)){
      var exclusionPlaceID = Array[String]("0")
      if(!deletionExclusionSite.toString.isEmpty){
        exclusionPlaceID = deletionExclusionSite.toString().split(",")
      }
      var placeNames = List[String]() // 現場名称

      // メール送信日の曜日を取得
      val deldate = Calendar.getInstance
      var countDay = noticeDaysAgo - 1;
      deldate.set(Calendar.DATE,(deldate.getActualMaximum(Calendar.DATE) - countDay))
      val week = deldate.get(Calendar.DAY_OF_WEEK)
      if(week == 1){
        // 日曜日の場合は月曜日にメールを送信
        countDay = countDay - 1
      }else if(week == 7){
        // 土曜日の場合は月曜日にメールを送信
        countDay = countDay - 2
      }

      val todayDf = new SimpleDateFormat("dd")
      val df = new SimpleDateFormat("yyyy-MM-")
      // 当月の最終日を取得
      val noticeCal = Calendar.getInstance
      val lastDayOfMonth = noticeCal.getActualMaximum(Calendar.DATE)
      var today = todayDf.format(noticeCal.getTime()).toInt
      if(noticeMailTestDate.toString.nonEmpty){
        // テスト確認用メール送信日が設定されていた場合
        today = noticeMailTestDate.toString.toInt
      }
      // メール送信日か否かをチェック
      if((lastDayOfMonth - countDay) == today) {
        if(checkMailAddressRegularity(sendMaileFromUser.toString)) {
          val cal = Calendar.getInstance
          val calDel = Calendar.getInstance
          val delMonth = (deleteInterval - 1) * -1
          calDel.add(Calendar.MONTH, delMonth); // 先月、先々月分は残す
          val delateTime = "01 00:00:00" // 1日固定
          val delateDate = df.format(calDel.getTime()).toString + delateTime

          // 削除開始対象月取得
          // 環境変数（３カ月）を減算
          val deletionPeriod = deleteInterval * -1
          cal.add(Calendar.MONTH, deletionPeriod);

          // 処理開始
          Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- start -- """)
          try {
            // DateFormat
            val ymdf = new SimpleDateFormat("yyyyMM")
            val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            // 削除開始対象月
            val targetMonth = ymdf.format(cal.getTime())

            val placeData = placeDAO.selectPlaceAll(WORKING_STATUS)
            placeData.zipWithIndex.foreach { case (place, i) =>
              // ログ削除除外現場チェック
              val exclusionId = exclusionPlaceID.find(_ == place.placeId.toString)
              if (place.btxApiUrl != null && place.btxApiUrl != "" && exclusionId.isEmpty) {
                val placeId = place.placeId

                val itemLogMinData = itemlogDAO.selectOldestRow(placeId)
                if (itemLogMinData != null && itemLogMinData.length > 0) {
                  // 最古のレコード　Date型変換
                  val lastMonth = new DateTime(sdf.parse(itemLogMinData.last.updatetime))
                  val lastYearMonth = lastMonth.toString("yyyyMM")
                  // 削除開始対象月が有るか？
                  if (lastYearMonth.toInt <= targetMonth.toInt) {
                    // 削除対象データ有り
                    val itemLogMinData = itemlogDAO.selectOldestRow(placeId, delateDate)
                    if (itemLogMinData != null && itemLogMinData.length > 0) {
                      // 仮設材ログ、予約管理削除メール通知
                      // 現場名称取得
                      placeNames = placeNames :+ place.placeName

                      var placeName = List[String]() // 現場名称
                      placeName = placeName :+ place.placeName
                      val mailer = new Mail
                      val mailinfo = new MailInfo(MAGTYPE_SITE_MANAGER, sendMaileFromUser.toString, "", placeName)
                      val users = userDAO.selectSendMailUserList(placeId)
                      for (user <- users) {
                        if (checkMailAddressRegularity(user.email.trim)) {
                          mailer.sendEmail(mailerClient, user, mailinfo)
                        } else {
                          Logger.error(s"""${user.email}は不正なメールアドレス""")
                        }
                      }
                    }
                  }
                }
              }
            }

            // メールを送信していた場合はdevelopにもメール送信する
            if (placeNames.nonEmpty) {
              val mailer = new Mail
              val users = userDAO.selectSendMailPermission(4)
              if (developMailAddress != null
                && developMailAddress.toString.nonEmpty
                && checkMailAddressRegularity(developMailAddress.toString.trim)) {
                val mailinfo = new MailInfo(MAGTYPE_DEVELOP, sendMaileFromUser.toString, developMailAddress.toString, placeNames)
                mailer.sendEmail(mailerClient, users.head, mailinfo)
              }
            }
          } catch {
            case e: Exception =>
              System.out.println("--------------------バッチエラー検知.start--------------------------")
              Logger.error(s"""${BATCH_NAME}にてエラーが発生""", e)
              System.out.println("--------------------バッチエラー検知.end--------------------------")
          }
        }
      }
    }
  }

  // メールアドレスチェック
  def checkMailAddressRegularity(address: String): Boolean = {
    var result = false
    val aText = "^(([0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*)|(\\\"[^\\\"]*\\\"))@[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*$"
    val regularExpression = aText
    result = checkMailAddress(address, regularExpression)
    return result
  }

  def checkMailAddress(address: String, regularExpression: String): Boolean = {
    val pattern = Pattern.compile(regularExpression)
    val matcher = pattern.matcher(address)
    if (matcher.find) {
      true
    }else{
      false
    }
  }
}
