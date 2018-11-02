package models.api

import actors.MailInfo
import models.User
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.Messages
import play.libs.mailer.{Email, MailerClient}


class Mail {
  def sendEmail(mailerClient: MailerClient, user: User, mailInfo: MailInfo)(implicit m: Messages): Unit = {
    val email = new Email
    val subjectMsg = Messages("mail.NoticeMail.subject")
    email.setSubject(subjectMsg)
    email.setFrom(mailInfo.fromUser)
    if(mailInfo.magType == 1){
      email.addTo(user.email)
      email.setBodyText(level3Body(mailInfo))
      mailerClient.send(email)
    }else if(mailInfo.magType == 2){
      if(mailInfo.userEmail.nonEmpty){
        email.addTo(mailInfo.userEmail)
        email.setBodyText(level4Body(mailInfo))
        mailerClient.send(email)
      }
    }
  }

  // 権限３のメッセージ
  def level3Body(mailInfo: MailInfo)(implicit m: Messages): String = {
    val bodyMessage = Messages("mail.NoticeMail.Body.level3.message")
    Logger.info(s"""${new DateTime().toString("yyyy/MM/dd HH:mm:ss.SSS")}  ${bodyMessage}""")
    bodyMessage
  }

  // 権限４のメッセージ
  def level4Body(mailInfo: MailInfo)(implicit m: Messages): String = {
    val body = new StringBuilder
    val bodyMessage = Messages("mail.NoticeMail.Body.level4.message")
    body.append(bodyMessage)
    for(placeName <- mailInfo.placeName){
      body.append("\r\n" + placeName)
    }
    body.toString()
  }
}
