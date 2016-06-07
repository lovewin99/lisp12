package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取mr数据字段信息
 * Created by wangxy on 16-1-6.
 */
object MrSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("MR.SEPRATOR", "")
  val mr_length = prop.getOrElse("mr_length", "100").toInt
  val index_imsi = prop.getOrElse("index_imsi", "100").toInt
  val index_imei = prop.getOrElse("index_imei", "100").toInt
  val index_msisdn = prop.getOrElse("index_msisdn", "100").toInt
  val index_groupid = prop.getOrElse("index_groupid", "100").toInt
  val index_code = prop.getOrElse("index_code", "100").toInt
  val index_s1ap = prop.getOrElse("index_s1ap", "100").toInt
  val index_enbid = prop.getOrElse("index_enbid", "100").toInt
  val index_cellid = prop.getOrElse("index_cellid", "100").toInt
  val index_time = prop.getOrElse("index_time", "100").toInt
  val index_ta = prop.getOrElse("index_ta", "100").toInt
  val index_aoa = prop.getOrElse("index_aoa", "100").toInt
  val index_sfreq = prop.getOrElse("index_sfreq", "100").toInt
  val index_spci = prop.getOrElse("index_spci", "100").toInt
  val index_srsrp = prop.getOrElse("index_srsrp", "100").toInt
  val index_neinum = prop.getOrElse("index_neinum", "100").toInt

  // 单个临区信息长度
  val nei_length = prop.getOrElse("nei_length", "100").toInt
  // 临区信息相对位置
  val neipci_index = prop.getOrElse("neipci_index", "100").toInt
  val neifreq_index = prop.getOrElse("neifreq_index", "100").toInt
  val neirsrp_index = prop.getOrElse("neirsrp_index", "100").toInt
}
