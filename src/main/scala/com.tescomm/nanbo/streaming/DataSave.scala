package com.tescomm.nanbo.streaming

import com.tescomm.utils.ConfigUtils
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Created by wangxy on 16-6-1.
 */
object DataSave {

  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      System.out.println("usage: <topic,numThread> <out-path> <isFromFirst>")
      System.exit(1)
    }

    val Array(topicinfo, outPath, isFromFirst) = args

    val conf = new SparkConf()
    if ("1" == isFromFirst)
      conf.set("auto.offset.reset ", "smallest")
    val ssc = new StreamingContext(conf, Seconds(300))

    val tinfo = topicinfo.trim.split(",",-1)
    val topic = Map(tinfo(0) -> tinfo(1).toInt)

    val prop = ConfigUtils.getConfig("/config/global.properties")
    val zkQuorum = prop.getOrElse("zkQuorum", "")

    KafkaUtils.createStream(ssc, zkQuorum, "2", topic).map(_._2).saveAsTextFiles(outPath)

    ssc.start()
    ssc.awaitTermination()
  }

}
