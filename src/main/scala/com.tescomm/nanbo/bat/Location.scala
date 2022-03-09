package com.tescomm.nanbo.bat

import com.tescomm.lisp.baseSchema.{MrSchema, mcSeqSchema}
import com.tescomm.nanbo.streaming.function.FingergGis
import com.tescomm.nanbo.summary.{LteAreaSum5Min, GridUser5Min}
import com.tescomm.tools.GetRandomGrid
import com.tescomm.utils.{RedisUtils, ConfigUtils}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.log4j.{Logger,Level}
import scala.collection.mutable.ArrayBuffer
import com.tescomm.utils.DateUtils



/**
 *spark-submit --master spark://cloud138:7077 --total-executor-cores 50 --executor-memory 15g --jars  \
jedis-2.1.0.jar,commons-pool-1.5.6.jar,redisclient_2.10-2.12.jar,commons-pool-1.5.6.jar \
--driver-class-path redisclient_2.10-2.12.jar:commons-pool-1.5.6.jar --class com.tescomm.nanbo.bat.Location \
lisp12_2.10-1.0.jar wxy/test20 wxy/test21 wxy/test22 wxy/test23 wxy/test24
 * Created by wangxy on 16-6-7.
 */
object Location {

  // 经纬度优先使用顺序 app, mr, s1-u
  val appindex = "1"
  val mrindex = "2"
  val s1uindex = "3"

  val nullSeq = "65535"

  def getCustomerMap(tableName: String, columns: String*) = {
    val prop = ConfigUtils.getConfig("/config/initredis.properties")
    val valueSep = prop.getOrElse(s"$tableName.valuelink", ",")
    val tableColumns = prop.getOrElse(s"$tableName.value", "").split(",",-1)
    RedisUtils.getResultMap(tableName).map { case (k, v) =>
      val lines = v.split(valueSep, -1)
      k -> columns.map(tableColumns.indexOf(_)).map(lines(_)).mkString("|")
    }
  }

  def main(args: Array[String]): Unit = {

    if (args.length != 6) {
      System.out.println("usage: <in-path> <out-path> <path1> <path2> <path3> <time>")
      System.exit(1)
    }

    //设置log级别
//    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
//    Logger.getLogger("org.eclipse.jetty.service").setLevel(Level.OFF)

    val Array(inPath, outPath, pathgrid, pathlocale, patharea, usetime) = args

//    val nowTime = DateUtils.getNowTime
    val endTime = DateUtils.getEndTime(usetime).replaceAll("[\\-: ]", "")
    val day = endTime.slice(0, 8)
    val hour = endTime.slice(8, 10)
    val minute = endTime.slice(10, 12)

    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val hadoopconf1 = sc.hadoopConfiguration
    hadoopconf1.set("mapred.output.compress", "false")
    hadoopconf1.set("mapred.compress.map.output", "false")
    // 手机归属地表(tel_locale_info)
    val tellib = RedisUtils.getResultMap("tel_locale_info")
    val tellib1 = sc.broadcast(tellib)
    // 栅格信息库(grid_info)
    val str = Array("area", "sub_area", "nature", "traffic_area", "custom_area")
    val gridinfolib = getCustomerMap("grid_info", str: _*)
    val gridinfolib1 = sc.broadcast(gridinfolib)
    // 栅格随机表(cell_gridlist)
    val sgRlib = RedisUtils.getResultMap("cell_gridlist")
    val sgRlib1 = sc.broadcast(sgRlib)
    // 指纹库表(fingerprint)
    val fingerlib = RedisUtils.getResultMap("fingerprint")
    val tfingerlib = fingerlib.map { x =>
      val arr = x._2.split("\\$", -1).map {
        _.split(",", -1)
      }
      (x._1, arr)
    }.toArray
    val fingerlib1 = sc.broadcast(tfingerlib)
    // cgipci_freq表
    val cpflib = RedisUtils.getResultMap("cgipci_freq")
    val cpflib1 = sc.broadcast(cpflib)
    // cgi_pci表
    val cplib = RedisUtils.getResultMap("cgi_pci")
    val cplib1 = sc.broadcast(cplib)

    if (tellib.size == 0 || gridinfolib.size == 0 || sgRlib.size == 0 || fingerlib.size == 0 || cpflib.size == 0 || cplib.size == 0) {
      System.out.println("check all lib !!!!!!!!!")
      System.exit(1)
    }

    val prop = ConfigUtils.getConfig("/config/global.properties")
    val zkQuorum = prop.getOrElse("zkQuorum", "")

    val lines = sc.textFile(inPath)

    /**
     * 根据数据字段数量判断数据来源
     *
     */
      val myrdd = lines.map {x =>
        try {
              val strArr = x.split("\\|", -1)
              strArr.length match {
                // mc s1-mme
                case mcSeqSchema.mcseq_length =>
                  val time = strArr(mcSeqSchema.mcseq_rtime)
                  val key = strArr(mcSeqSchema.mcseq_msisdn) + "," + strArr(mcSeqSchema.mcseq_imsi)
                  val eci = strArr(mcSeqSchema.mcseq_ci)
                  (key, (time, eci, (s1uindex, strArr)))
                // mr数据
                case MrSchema.mr_length =>
                  val time = strArr(MrSchema.index_time)
                  val key = strArr(MrSchema.index_msisdn) + "," + strArr(MrSchema.index_imsi)
                  if (strArr(MrSchema.index_enbid) != nullSeq && strArr(MrSchema.index_cellid) != nullSeq) {
                    val eci = strArr(MrSchema.index_enbid).toInt << 8 | strArr(MrSchema.index_cellid).toInt
                    //            (msisdn,(mrindex+","+time, strArr))
                    (key, (time, eci.toString, (mrindex, strArr)))
                  } else {
                    ("", ("", "", ("", Array[String]())))
                  }
                case _ => ("", ("", "", ("", Array[String]())))
              }
        } catch {
          case e: Exception => e.printStackTrace(); ("", ("", "", ("", Array[String]())))
        }
      }.filter(_._1 != "").groupBy(_._1).map { x =>
        try {
          x match {
            case (msisdnk, v) =>
              val data = v.map{_._2}

              /**
               * 先安时间对数据排序
               * 时间顺序上 连续相同的线序 安优先级排序
               */
              var info = ArrayBuffer[(String, Array[String])]()
              var tmpInfo = ArrayBuffer[(String, Array[String])]()
              // 时间排序
              val timeData = data.toArray.sortBy(_._1).reverse
              var tmpEci = timeData.head._2
              timeData.foreach {
                case (time1, eci1, data1) =>
                  if (eci1 == tmpEci) {
                    tmpInfo += data1
                  } else {
                    // 相同小区的优先级排序
                    info ++= tmpInfo.sortBy(_._1)
                    tmpInfo.clear()
                    tmpEci = eci1
                    tmpInfo += data1
                  }
              }
              if (tmpInfo.length != 0) {
                info ++= tmpInfo.sortBy(_._1)
              }

              // 通过长度判断不同的数据按照不同的定位算法
              var index = 0
              var res = Array[String]()
              while (index < info.length && res.length == 0) {
                val strArr = info(index)._2
                index += 1
                strArr.length match {
                  // mc s1-mme
                  case mcSeqSchema.mcseq_length =>
                    val msisdn = strArr(mcSeqSchema.mcseq_msisdn)
                    val Array(lon, lat, x, y) = GetRandomGrid.getLonLatXY(strArr(mcSeqSchema.mcseq_lac), strArr(mcSeqSchema.mcseq_ci), sgRlib1.value)
                    // 获取区域信息
                    val sg = x + "," + y
                    val info = gridinfolib1.value.getOrElse(sg, "||||")
//                    val info = "4|110|||"
                    // 活取归属地信息
                    val localeid = tellib1.value.getOrElse(msisdn.slice(0, 7), "")
                    if ("||||" != info && "" != localeid && nullSeq != strArr(mcSeqSchema.mcseq_imsi) && nullSeq != msisdn) {
                      res = Array[String](strArr(mcSeqSchema.mcseq_rtime), strArr(mcSeqSchema.mcseq_imsi), msisdn, localeid, info, lon, lat, x, y)
                    }
                  case MrSchema.mr_length =>
                    if (strArr(MrSchema.index_srsrp) != "0" && strArr(MrSchema.index_srsrp) != nullSeq &&
                      strArr(MrSchema.index_enbid) != nullSeq && strArr(MrSchema.index_cellid) != nullSeq) {
                      val eci = strArr(MrSchema.index_enbid).toInt << 8 | strArr(MrSchema.index_cellid).toInt
                      val spci = cplib1.value.getOrElse(eci.toString, "")

                      var sfreq = strArr(MrSchema.index_sfreq)
                      if(sfreq == nullSeq){
                        sfreq = cpflib1.value.getOrElse(eci.toString+","+spci, "")
                      }
                      if(sfreq != "" && sfreq != nullSeq){
                        val fingerArr = ArrayBuffer[ArrayBuffer[String]]()
                        val flag = spci + "|" + sfreq
                        val ta = "0"
                        val rsrp = strArr(MrSchema.index_srsrp).toInt - 140
                        fingerArr += ArrayBuffer[String](flag, ta, "1", rsrp.toString)
                        for (i <- 0 to strArr(MrSchema.index_neinum).toInt - 1) {
                          val cur = MrSchema.index_neinum + i * MrSchema.nei_length
                          val pci = strArr(cur + MrSchema.neipci_index)
                          val freq = cpflib1.value.getOrElse(eci.toString+","+pci, "")
                          val neiRsrp = strArr(cur + MrSchema.neirsrp_index)
                          if(nullSeq != pci && "" != freq && nullSeq != neiRsrp){
                            val neiPci_freq = pci + "|" + freq
                            fingerArr += ArrayBuffer[String](neiPci_freq, ta, "0", neiRsrp)
                          }
                        }
                        val (x, y) = FingergGis.location(fingerArr, fingerlib1.value)
                        val (lon, lat) = FingergGis.Mercator2lonlat(x.toInt * 20, y.toInt * 20)
                        // 获取区域信息
                        val sg = x + "," + y
                        val info = gridinfolib1.value.getOrElse(sg, "||||")
                        // 活取归属地信息
                        val msisdn = strArr(MrSchema.index_msisdn)
                        val localeid = tellib1.value.getOrElse(msisdn.slice(0, 7), "")
                        val time = strArr(MrSchema.index_time)
                        val imsi = strArr(MrSchema.index_imsi)
                        if ("||||" != info && "" != localeid && nullSeq != imsi && nullSeq != msisdn) {
                          //                  val imei = strArr(MrSchema.index_imei)
                          res = Array[String](time, imsi, msisdn, localeid, info, lon.toString, lat.toString, x.toString, y.toString)
                        }
                      }
                    }
                }
              }
              res
          }
        }
        catch {
          case e: Exception => e.printStackTrace(); Array[String]()
        }
      }.filter(_.length != 0)
      myrdd.cache()

      // 数据落地保存
      val path = Array[String](outPath, day, hour, minute).mkString("/")
      myrdd.map(_.mkString(",")).saveAsTextFile(path)

      // 5分钟栅格汇总
      val path1 = Array[String](pathgrid, day, hour, minute).mkString("/")
      GridUser5Min.GridSumPersion(myrdd, path1, usetime)

      // 归属地汇总
      val path2 = Array[String](pathlocale, day, hour, minute).mkString("/")
      LteAreaSum5Min.sumProcess1(myrdd, path2, usetime)

      // 区域5分钟汇总
      val path3 = Array[String](patharea, day, hour, minute).mkString("/")
      LteAreaSum5Min.sumProcess2(myrdd, path3, usetime)
  }

}
