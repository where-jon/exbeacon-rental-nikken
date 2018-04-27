package utils

import models.{FloorInfo, exCloudBtxData}

object BtxUtil {
  /**
    * APIデータからフロアを検出する（仮設材用）
    *
    */
  def getNearestFloor[A](floorInfoList: Seq[FloorInfo], apiData: exCloudBtxData): Seq[FloorInfo] = {
    val floor = floorInfoList.filter(_.exbDeviceIdList contains apiData.device_id.toString)
    if(floor.nonEmpty){
      var newFloorList = Seq[FloorInfo]()
      apiData.nearest.foreach { btx =>
        val fl:Seq[FloorInfo] = floorInfoList.filter(_.exbDeviceIdList contains btx.device_id.toString)
        newFloorList = newFloorList union fl
      }
      if(newFloorList.isEmpty){
        floor
      }else{
        var result = Seq[FloorInfo]()
        newFloorList.foreach{ f =>
          if(newFloorList.filter(_.floorId == f.floorId).length > 1){
            result :+= f
          }
        }
        if(result.nonEmpty){
          result
        }else{
          floor
        }
      }
    }else{
      floor
    }
  }


}