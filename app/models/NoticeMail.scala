package models.api

import actors.MailInfo
import models.User
import org.joda.time.DateTime
import play.api.Logger
import play.libs.mailer.{Email, MailerClient}


class Mail {

  val BATCH_NAME = "メール送信"
  val MAGTYPE_DEVELOP = "develop"  // 開発者
  val MAGTYPE_SITE_MANAGER = "SiteManager"  // 現場責任者
  val subjectMessage = "仮設材ログ削除実施のお知らせ"
  val level3BodyMessage = "来月１日の深夜０時に仮設材ログ、及び予約情報について３ヶ月前の削除処理を実施しますので、ご確認を宜しくお願い致します。"
  val level3BodyPlaceTitle = "【現場】"
  val level4BodyMessage = "来月１日の深夜０時に下記の現場の仮設材ログ、及び予約情報について３ヶ月前の削除処理を実施しますので、ご確認を宜しくお願い致します。"
  val level4BodyPlaceTitle = "【対象の現場】"
  val nakapochi = "・"

  def sendEmail(mailerClient: MailerClient, user: User, mailInfo: MailInfo): Unit = {
    val email = new Email
    email.setSubject(subjectMessage)
    email.setFrom(mailInfo.fromUser.trim)
    if(mailInfo.magType.equals(MAGTYPE_SITE_MANAGER)){
      email.addTo(user.email.trim)
      email.setBodyText(level3Body(mailInfo))
      mailerClient.send(email)
      Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- ${user.name}<${user.email}>にメール送信しました。 --- """)
    }else if(mailInfo.magType.equals(MAGTYPE_DEVELOP)){
      if(mailInfo.userEmail.nonEmpty){
        email.addTo(mailInfo.userEmail.trim)
        email.setBodyText(level4Body(mailInfo))
        mailerClient.send(email)
        Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${BATCH_NAME} --- developer<${mailInfo.userEmail}>にメール送信しました。 --- """)
      }
    }
  }

  // 権限３のメッセージ
  def level3Body(mailInfo: MailInfo) = {
    val body = new StringBuilder
    val bodyMessage = level3BodyMessage
    body.append(bodyMessage)
    body.append("\r\n" + level3BodyPlaceTitle)
    for(placeName <- mailInfo.placeName){
      body.append("\r\n" + nakapochi + placeName)
    }
    body.toString()
  }

  // 権限４のメッセージ
  def level4Body(mailInfo: MailInfo) = {
    val body = new StringBuilder
    val bodyMessage = level4BodyMessage
    body.append(bodyMessage)
    body.append("\r\n" + level4BodyPlaceTitle)
    for(placeName <- mailInfo.placeName){
      body.append("\r\n" + nakapochi + placeName)
    }
    body.toString()
  }
}
