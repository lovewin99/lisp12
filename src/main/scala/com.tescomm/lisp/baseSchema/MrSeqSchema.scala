package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取mr序列数据字段信息
 * Created by wangxy on 16-1-6.
 */
object MrSeqSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("MRSEQ.SEPRATOR", "")
  val mrseq_length = prop.getOrElse("mrseq_length", "100").toInt
  val mrseq_type = prop.getOrElse("mrseq_type", "100").toInt
  val mrseq_stime = prop.getOrElse("mrseq_stime", "100").toInt
  val mrseq_etime = prop.getOrElse("mrseq_etime", "100").toInt
  val mrseq_rtime = prop.getOrElse("mrseq_rtime", "100").toInt
  val mrseq_imsi = prop.getOrElse("mrseq_imsi", "100").toInt
  val mrseq_msisdn = prop.getOrElse("mrseq_msisdn", "100").toInt
  val mrseq_imei = prop.getOrElse("mrseq_imei", "100").toInt
  val mrseq_enbid = prop.getOrElse("mrseq_enbid", "100").toInt
  val mrseq_cellid = prop.getOrElse("mrseq_cellid", "100").toInt
  val mrseq_count = prop.getOrElse("mrseq_count", "100").toInt
}
