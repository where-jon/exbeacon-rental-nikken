package models.site

import javax.inject.Inject
import play.api.db._

case class TxSearchForm(powerValue: String,itemTypeId:String)

@javax.inject.Singleton
class TxBatteryDAO @Inject() (dbapi: DBApi) {
}

