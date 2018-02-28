package utils.silhouette

import models.User
import Implicits._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import scala.concurrent.Future
import javax.inject.Inject

class UserService @Inject() (userDAO: models.UserDAO) extends IdentityService[User] {
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.findByEmail(loginInfo)
  def save(user: User): Future[User] = userDAO.save(user)
}