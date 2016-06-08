package com.tescomm.nanbo.summary

import com.tescomm.tools.SQLTools
import com.tescomm.utils.{ConfigUtils, DateUtils}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

/**
 * Created by s 2016-05-25.
 */
object CharmHour {

  val prop = ConfigUtils.getConfig("/config/nanbo-charm.properties")
  val keys = Array("imsi", "msisdn", "time", "areaId")
  val Seq(imsii, msisdni, timei, areaIdi) = ConfigUtils.getProps2Int(prop, keys: _*)

  /**
   * imsi,msisdn,time,areaId
   */
  object user {
    def unapply(str: String): Option[(String, String, String, String)] = {
      val arr = str.split(",")
      val areas = arr(areaIdi).split("\\|")
      Some(arr(imsii), arr(msisdni), arr(timei), areas(1))
    }
  }


  /**
   * spark-submit --master spark://cloud138:7077 --class com.tescomm.nanbo.summary.CharmHour \
     --driver-class-path mysql-connector-java-5.1.37.jar \
     --jars mysql-connector-java-5.1.37.jar lisp_2.10-1.0.jar \
     /user/etl/nanbo/location5 /user/etl/nanbo/charmHour/hourOut /user/etl/nanbo/charmHour/result 20160525/10
   * @param args inputPath hourOutputPath outputPath
   */
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Charm hour")
    val sc = new SparkContext(conf)

    val Array(inputPath, hourOutputPath, outputPath, hour) = args
    val second = 1000 //60000
    val pavIds = prop.getOrElse("nanbo.pav.ids", "").split(",")
    val rdd = sc.textFile(s"$inputPath/$hour/*/")

    /**
     * 计算某用户在某场馆停留时长
     * 1. 读取数据，提取imsi，msisdn，time， 子区域id
     * 2. 以imsi，msisdn做key， 时间毫秒数，子区域id做value
     * 3. 按照key分组
     * 4. 将value按照时间排序
     * 5. 计算出用户在每个子区域停留时长
     * 6. 过滤子区域为场馆的数据
     * 7. 按照场馆分组求和，既得用户在该场馆的停留时长
     * 8. 返回imsi，msisdn，场馆id，时长毫秒数/60000转化为分钟
     */
    rdd.map {
      case user(imsi, msisdn, time, areaId) => ((imsi, msisdn), (DateUtils.getUnixTime(time), areaId))
    }.groupByKey().mapPartitions { iter =>
      iter.flatMap { case (k, v) =>
        val list = v.toList.sortBy(_._1)
        (list.tail, list.init).zipped.map { case (a, b) =>
          (b._2, a._1 - b._1)
        }.filter(x => pavIds.contains(x._1))
          .groupBy(_._1).map { case (areaId, duration) =>
          val total = (0L /: duration)(_ + _._2)
          s"${k._1},${k._2},$areaId,${total.toLong / second}"
        }
      }
    }.saveAsTextFile(s"$hourOutputPath/$hour/")

    /**
     * imsi,msisdn,areaId,duration
     */
    object hoursInfo{
      def unapply(str: String): Option[(String, String, String, Long)] ={
        val arr = str.split(",")
        Some(arr(0), arr(1), arr(2), arr(3).toLong)
      }
    }

    /**
     * 魅力指数小时汇总
     * 1. 读取之前小时的所有数据
     * 2. 和前一小时数据union
     * 3. 用imsi，msisdn，areaId做key，duration做value，对时长求和
     * 4. 用areaId做key，人设置成1L，和时长做value，多列相加求和
     * 5. 输出结果保存到指定路径
     */
    val insertSql = prop.getOrElse("nanbo.charm.cur.insert.sql", "")
    val retTime = hour.replaceAll("(\\d{4})(\\d{2})(\\d{2})/(\\d{2})", "$1-$2-$3 $4:00:00.000")
    val hoursRdd = sc.textFile(s"$hourOutputPath/${hour.substring(0, 8)}/*")
    val retRdd = hoursRdd.map{
      case hoursInfo(imsi, msisdn, areaId, duration) => ((imsi, msisdn, areaId), duration)
    }.reduceByKey(_ + _).map(x => (x._1._3, List(1L, x._2)))
      .reduceByKey((a, b) => (a, b).zipped.map(_+_)).mapPartitions{ iter =>
        iter.map{ ret =>
//          s"$retTime,${ret._1},${ret._2.head},${ret._2.last / ret._2.head / 60}"
          Seq(retTime, ret._1, ret._2.head.toString, (ret._2.last / ret._2.head / 60).toString)
        }

    }.map(SQLTools.replaceSQL(insertSql, _))

//    JDBCUtils.update(ConfigUtils.getConfig("/config/global.properties").getOrElse("DATA_BASE", ""), retRdd)

    retRdd.saveAsTextFile(s"$outputPath/$hour")

  }
}
