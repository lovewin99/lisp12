package com.tescomm.tools

import scala.math._

/**
 * 根据tac eci获取栅格
 * Created by wangxy on 16-1-15.
 */
object GetRandomGrid {
  /**
   * 根据tac和eci在栅格库中随机取栅格
   * @param tac
   * @param eci
   * @param gridMap
   * @return
   */
  def getLonLatXY(tac: String, eci:String, gridMap: Map[String, String]): Array[String] = {
    val key = tac + "," + eci
    gridMap.get(key) match{
      case Some(info) => {
        val strArr = info.split(",", -1)
        val index = rint(random * (strArr.length - 1)).toInt
        strArr(index).split("\\|", -1)
      }
      case _ => Array("","","","")
    }
  }
}
