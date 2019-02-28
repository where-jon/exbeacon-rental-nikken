package models.manage

import javax.inject.Inject
import play.api.db._

case class UserLevelEnum(
  map: Map[Int, String] = Map[Int,String] (
    1 -> "一般",
    2 -> "管理者"
  )
)
/**
  * アカウント新規作成時のForm
  */
case class AccountCreateForm(
  userName: String, // アカウント名
  userLoginId: String, // アカウントID
  userLevel: String, // アカウント権限
  userPassword1: String, // パスワード
  userPassword2: String // 確認用パスワード
)

/**
  * アカウント更新時のForm
  */
case class AccountUpdateForm(
  userId: String, // アカウントID
  userName: String, // アカウント名
  userLoginId: String, // ログインID
  userLevel: String // アカウント権限
)

/**
  * アカウントパスワード更新時のForm
  */
case class AccountPasswordUpdateForm(
  userId: String,
  userPassword1: String,
  userPassword2: String
)

/**
  * アカウント削除時のForm
  */
case class AccountDeleteForm(
  userId: String
)



@javax.inject.Singleton
class Account @Inject()(dbapi: DBApi) {

}

