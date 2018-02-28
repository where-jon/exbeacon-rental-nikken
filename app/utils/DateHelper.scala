package utils

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar

object DateHelper {
  def toString(date: Option[Date], format: String): String = {
    if (date != None) {
      val sdf = new SimpleDateFormat(format)
      sdf.format(date.get)
    } else {
      ""
    }
  }
  def toString(date: Date, format: String): String = {
    val sdf = new SimpleDateFormat(format)
    sdf.format(date)
  }
  // 月末日を取得
  def getLastDay(date: Date): Int = {
    val cal = Calendar.getInstance
    cal.setTime(date)
    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }
}