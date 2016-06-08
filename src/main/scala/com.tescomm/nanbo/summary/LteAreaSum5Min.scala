package com.tescomm.nanbo.summary

import com.tescomm.utils.{DateUtils, ConfigUtils}
import org.apache.spark.rdd.RDD

/**
  * Created by sjl on 2016/5/24.
  * RDD数据：0 time,1 imsi,2 msisdn(手机号码),3 (归属地id),4 (归属地id 不同id间用"|"分隔),5 lon(经度),6 lat(纬度),7 x,8 y
  * spark-submit --master spark://cloud138:7077  --total-executor-cores 2 --class com.tescomm.nanbo.summary.LteAreaSum5Min
  */
object LteAreaSum5Min {
  val prop = ConfigUtils.getConfig("/config/global.properties")
  val LOCAL_AREA_ID = prop.getOrElse("LOCAL_AREA_ID", "")
  val DATA_BASE = prop.getOrElse("DATA_BASE", "")

  /** 区域及归属地汇总 */
  def sumProcess1(dataRDD: RDD[Array[String]], path: String, usetime: String) = {
    val str = s"insert into area_source_5 values ('#', '#', '#', '#', #)"
//    val nowDate = DateUtils.getNowTime
    val time = DateUtils.getEndTime(usetime)
    val calcSize = "5分钟"

    val resultRdd = dataRDD.flatMap { x =>
      val imsi_msisdn = x(1) + "_" + x(2)
      val localId = x(3)
      val areaId = x(4)
      areaId.split("\\|").map(id => ((id, localId), imsi_msisdn)).filter(_._1._1 != "")
    }.groupBy(_._1).map {
        case ((areaId, localId), v) =>
          val iter_msisdn = v.map{_._2}
          Array(areaId, time, calcSize, localId, iter_msisdn.toSet.size.toString).mkString(",")
    }.saveAsTextFile(path)
  }

  /** 区域汇总(本外) */
  def sumProcess2(dataRDD: RDD[Array[String]], path: String, usetime: String) = {
    val str = s"insert into area_count_5 values ('#', '#', #)"
//    val nowDate = DateUtils.getNowTime
    val time = DateUtils.getEndTime(usetime)

    val resultRdd = dataRDD.flatMap { x =>
      val imsi_msisdn = x(1) + "_" + x(2)
      val localId = if (x(3) == LOCAL_AREA_ID) "1" else "2"
      val areaId = x(4)
      areaId.split("\\|").map(id => (id, (localId, imsi_msisdn))).filter(_._1 != "")
    }.groupBy(_._1).map {
        case (areaId, v) =>
          val iter_msisdn = v.map{_._2}
          val msisdns = iter_msisdn.toSet
          val total_cnt = msisdns.size
          val field_cnt = msisdns.count(_._1 == "2")
          val local_cnt = total_cnt - field_cnt
          Array(areaId, time, total_cnt.toString).mkString(",")
    }.saveAsTextFile(path)
  }


}
