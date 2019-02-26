package models
// フォーム定義
case class PlaceChangeForm(inputPlaceId: String)
case class CmsLoginForm(inputPlaceId: String, inputCmsLoginPassword: String, inputReturnPath:String)
case class PlaceRegisterForm(
  placeName: String, // 現場名
  placeStatus: String, // 状態
  userLoginId: String, // 現場責任者ログインID
  userName: String, // 現場責任者名
  userPassword1: String, // パスワード
  userPassword2: String // 確認用パスワード
)
case class PlaceUpdateForm(
  placeId: String,
  userId: String,
  placeName: String,
  placeStatus: String,
  userName: String,
  userLoginId: String
)
case class PasswordUpdateForm(
  placeId: String,
  userId: String,
  password1: String,
  password2: String
)
case class PlaceDeleteForm(deletePlaceId: String)
case class PlaceSortForm(placeSortId: String)


