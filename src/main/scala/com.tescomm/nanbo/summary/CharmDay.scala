package com.tescomm.nanbo.summary

import com.tescomm.tools.SQLTools
import com.tescomm.utils.{ConfigUtils, JDBCUtils}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

import scala.collection.mutable.ArrayBuffer

/**
 * create by bao 2016-05-26
 */
object CharmDay {

  /**
   * spark-submit --master spark://cloud138:7077 --class com.tescomm.nanbo.summary.CharmDay \
     --driver-class-path mysql-connector-java-5.1.37.jar \
     --jars mysql-connector-java-5.1.37.jar lisp_2.10-1.0.jar
     /user/etl/nanbo/charmHour/hourOut /user/etl/nanbo/charmDay/result 20160525
   * @param args inputPath hourOutputPath outputPath
   */
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Charm hour").setMaster("local")
    val sc = new SparkContext(conf)

    val Array(hourInputPath, dayOutputPath, day) = args

    /**
     * imsi,msisdn,areaId,duration
     */
    object hoursInfo{
      def unapply(str: String): Option[(String, String, String, Long)] ={
        val arr = str.split(",")
        Some(arr(0), arr(1), arr(2), arr(3).toLong)
      }
    }

    val dayRdd = sc.textFile(s"$hourInputPath/$day/*")
    dayRdd.map{
      case hoursInfo(imsi, msisdn, areaId, duration) => (Seq(imsi, msisdn, areaId), duration)
    }.reduceByKey(_ + _).mapPartitions{ iter =>
      iter.map { case (k, v) =>
        val ab = new ArrayBuffer[Any]()
        ab ++= k
        ab += v
        ab.mkString(",")
      }
    }.saveAsTextFile(s"$dayOutputPath/$day")

    /**
     * 魅力指数小时汇总
     * 1. 读取之前小时的所有数据
     * 2. 和前一小时数据union
     * 3. 用imsi，msisdn，areaId做key，duration做value，对时长求和
     * 4. 用areaId做key，人设置成1L，和时长做value，多列相加求和
     * 5. 输出结果保存到指定路径
     */
    val prop = ConfigUtils.getConfig("/config/nanbo-charm.properties")
    val insertSql = prop.getOrElse("nanbo.charm.acc.insert.sql", "")
    val retTime = day.replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3 00:00:00.000")
    val daysRdd = sc.textFile(s"$dayOutputPath/*/*")
    val retRdd = daysRdd.map{
      case hoursInfo(imsi, msisdn, areaId, duration) => ((imsi, msisdn, areaId), duration)
    }.reduceByKey(_ + _).map(x => (x._1._3, List(1L, x._2)))
      .reduceByKey((a, b) => (a, b).zipped.map(_+_)).mapPartitions{ iter =>
        iter.map{ ret =>
          Seq(retTime, ret._1, ret._2.head.toString, (ret._2.last / ret._2.head / 60).toString)
//          s"$retTime,${ret._1},${ret._2.head},${ret._2.last / ret._2.head / 60}"
        }
    }.map(SQLTools.replaceSQL(insertSql, _))

    JDBCUtils.update(ConfigUtils.getConfig("/config/global.properties").getOrElse("DATA_BASE", ""), retRdd)

      //.saveAsTextFile(s"$outputPath/$day")

  }
}
