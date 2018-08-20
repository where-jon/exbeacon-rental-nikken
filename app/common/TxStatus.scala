package models

/**
 * TXの状態クラス
 */
class TxStatus() {
  val NOT_DETECTED: Int = 0 // 未検知
  val ABSENCE: Int = 1 // 不在
  val PRESENCE: Int = 2 // 在席

  private var status = NOT_DETECTED
  def setTxStatus(statusNum: Int) {
    status = statusNum
  }

  def getTxStatus(): Int = {
    status
  }
}
