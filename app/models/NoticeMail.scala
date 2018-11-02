package models.api

import actors.MailInfo
import models.User
import play.api.i18n.Messages
import play.libs.mailer.{Email, MailerClient}


class Mail {
  def sendEmail(mailerClient: MailerClient, user: User, mailInfo: MailInfo)(implicit m: Messages): Unit = {
    val email = new Email
    email.setSubject(Messages("mail.NoticeMail.subject"))
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
    Messages("mail.NoticeMail.Body3")
  }

  // 権限４のメッセージ
  def level4Body(mailInfo: MailInfo)(implicit m: Messages): String = {
    val body = new StringBuilder
    body.append(Messages("mail.NoticeMail.Body4"))
    for(placeName <- mailInfo.placeName){
      body.append("\r\n" + placeName)
    }
    body.toString()
  }
}











