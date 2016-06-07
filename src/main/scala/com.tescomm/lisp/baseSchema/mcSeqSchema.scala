package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取mc数据字段信息
 * Created by wangxy on 16-1-6.
 */
object mcSeqSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("MCSEQ.SEPRATOR", "")
  val mcseq_length = prop.getOrElse("mcseq_length", "100").toInt
  val mcseq_type = prop.getOrElse("mcseq_type", "100").toInt
  val mcseq_stime = prop.getOrElse("mcseq_stime", "100").toInt
  val mcseq_etime = prop.getOrElse("mcseq_etime", "100").toInt
  val mcseq_rtime = prop.getOrElse("mcseq_rtime", "100").toInt
  val mcseq_imsi = prop.getOrElse("mcseq_imsi", "100").toInt
  val mcseq_msisdn = prop.getOrElse("mcseq_msisdn", "100").toInt
  val mcseq_imei = prop.getOrElse("mcseq_imei", "100").toInt
  val mcseq_lac = prop.getOrElse("mcseq_lac", "100").toInt
  val mcseq_ci = prop.getOrElse("mcseq_ci", "100").toInt
  val mcseq_count = prop.getOrElse("mcseq_count", "100").toInt
}
