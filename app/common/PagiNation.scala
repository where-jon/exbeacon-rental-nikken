package common

import javax.inject.Inject

import models.CarViewer
import play.api.Configuration

/**
 * 共通ページングクラス
 */

class PagiNation @Inject() (config: Configuration) {
  val PAGE_LINE_COUNT = config.getInt("web.positioning.pageLineCount").getOrElse(20)
  var MAX_PAGE = 0;
  var PAGE = 1

  /** 作業車稼働状況分析用 */
  def getMovementPageData( getData:Seq[CarViewer]) : Seq[CarViewer] = {
    val pagePosition = (PAGE - 1) * PAGE_LINE_COUNT
    if(getData.length % PAGE_LINE_COUNT > 0){
      MAX_PAGE = getData.length / PAGE_LINE_COUNT + 1
    }else{
      MAX_PAGE = getData.length / PAGE_LINE_COUNT
    }
    val logItemList = getData.drop(pagePosition).take(PAGE_LINE_COUNT)
    return logItemList

  }
}
