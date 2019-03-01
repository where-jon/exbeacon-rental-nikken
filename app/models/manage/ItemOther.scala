package models.manage

import javax.inject.Inject
import play.api.db._

// フォーム定義
case class ItemDeleteForm(
  deleteItemOtherId: String
  , deleteItemTypeId: String
)
case class ItemUpdateForm(
    inputPlaceId: String
  , inputItemOtherId: String
  , inputItemOtherBtxId: String
  , inputItemOtherNo: String
  , inputItemOtherName: String
  , inputItemNote: String
  , inputItemTypeName: String
  , inputItemTypeId: String
)

