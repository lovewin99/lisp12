package com.tescomm.nanbo.summary

/**
 * Created by gh
 * 2016-05-27.
 */

import com.tescomm.utils.{DateUtils, ConfigUtils, JDBCUtils}

import org.apache.spark.rdd.RDD


object GridUser5Min {

  //有效数据标志位置
  val task = 0
  //有效数据标志值
  val taskData = "1"
  //归属地id 分隔符
  val splitBy = "\\|"
  //子区域在区域id的位置
  val subAreaTask = 1
  // 结果表
  val resultTable = "grid_person_sum"
  //读取配置文件
  val prop = ConfigUtils.getConfig("/config/global.properties")
  val LOCAL_AREA_ID = prop.getOrElse("LOCAL_AREA_ID", "")
  val DATA_BASE = prop.getOrElse("DATA_BASE", "")

  //提取器
  def unapply(str: Array[String]) = {
    //0 time,1 imsi,2 msisdn,3 localeid,4 areaid,5 lon,6 lat,7 x,8 y
    val s = str
    val areaIds = s(4).split(splitBy)
    val areaId = areaIds(task)
    //if (areaId == taskData) {
      Some(s(1), s(2), s(3), s(4), s(5), s(6), s(7), s(8))
    //}
 //   else {
   //   None
    //}
  }

  //5分钟栅格人数
  def GridSumPersion(rdd: RDD[Array[String]],path: String, usetime: String) = {
//    val nowDate = DateUtils.getNowTime
//    println(s"nowDate:  $nowDate")
    val endTime = DateUtils.getEndTime(usetime)
//    println(s"endTime: $endTime")
    val extractorRDD = rdd.mapPartitions { rs =>
      rs map {
        case GridUser5Min(imsi, msisdn, localeid, areaid, lon, lat, x, y) =>
          (endTime, imsi + "," + msisdn, localeid, areaid, lon, lat, x, y)
        //case _ => ("", "", "", "", "", "", "", "")
      }
    }
    val result = extractorRDD.filter { eds => eds._1 != "" }.map { eds =>
      val areaIds = eds._4.toString.split(splitBy)
      val subAreaId = areaIds(subAreaTask)
      //k=(time,lon,lat,x,y,areaId,subareaId)

      (Seq(eds._1.toString, eds._5.toString, eds._6.toString, eds._7.toString, eds._8.toString, eds._4.toString, subAreaId), Seq(eds._2))
    }.groupBy(_._1).map { case (k, v1) =>
      val v = v1.map{_._2}
      val vs = v.toSet
      //s"insert into $resultTable values ('${k.head}',${k(1)},${k(2)},${k(3)},${k(4)},'${k(5)}','${k(6)}',${vs.size})"
      s"${k.head},${k(1)},${k(2)},${k(3)},${k(4)},${k(5)},${k(6)},${vs.size}"
    }
    // JDBCUtils.update(DATA_BASE, result)
    result.saveAsTextFile(path)
  }

}
