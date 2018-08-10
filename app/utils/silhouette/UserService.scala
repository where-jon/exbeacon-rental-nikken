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
  def selectSuperUserList(): Seq[User] = userDAO.selectSuperUserList()
  def insert(user: User): Future[User] = userDAO.insert(user)
  def deleteLogicalByPlaceId(placeId: Int) = userDAO.deleteLogicalByPlaceId(placeId)
  def changePasswordByEmail(userEmail: String, passwd: String) = userDAO.changePasswordByEmail(userEmail: String, passwd: String)
  def changePasswordById(userId: String, passwd: String) = userDAO.changePasswordById(userId: String, passwd: String)
  def selectAccountByPlaceId(placeId: Int) = userDAO.selectAccountByPlaceId(placeId: Int)
  def updateUserNameByEmail(userId: String, userName: String) = userDAO.updateUserNameByEmail(userId: String, userName: String)
  def updateUserNameLevelById(userId: String, userName: String, level: String) = userDAO.updateUserNameLevelById(userId: String, userName: String, level: String)
  def deleteLogicalById(userId: String) = userDAO.deleteLogicalById(userId)
}
