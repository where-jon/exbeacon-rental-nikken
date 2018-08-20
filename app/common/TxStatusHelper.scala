package models

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ ZoneId, ZonedDateTime }
import javax.inject.Inject

import play.api.Configuration

/**
 * TXの状態判定クラス
 */
class TxStatusHelper @Inject() (config: Configuration, txStatus: models.TxStatus) {
  val POS_NONE: Int = -1 // 未検知判定
  private val LOST_TIME_MILLIS = config.getInt("web.positioning.absenceMinute").getOrElse(0) // 不在になる時間

  def getTxStatus(posId: Int, updateTime: String): TxStatus = {
    if (isNotDetected(posId)) {
      txStatus.setTxStatus(txStatus.NOT_DETECTED)
    } else if (isAbsence(updateTime)) {
      txStatus.setTxStatus(txStatus.ABSENCE)
    } else {
      txStatus.setTxStatus(txStatus.PRESENCE)
    }
    txStatus
  }

  def getTxStatusNotDetected(): TxStatus = {
    txStatus.setTxStatus(txStatus.NOT_DETECTED)
    txStatus
  }

  private def isNotDetected(posId: Int): Boolean = {
    if (posId == POS_NONE) true else false
  }

  private def isAbsence(updateTime: String): Boolean = {
    if (LOST_TIME_MILLIS == 0) {
      // ステータスを使用しない場合用
      false
    } else if (updateTime == "") {
      true
    } else {
      val today = ZonedDateTime.now();
      val updateParsetime = ZonedDateTime.parse(updateTime, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()))
      val localDiffMsec = ChronoUnit.MINUTES.between(today, updateParsetime)
      // 最終検知から指定時間経過している場合、不在
      if (localDiffMsec <= -LOST_TIME_MILLIS) true else false
    }
  }

}
