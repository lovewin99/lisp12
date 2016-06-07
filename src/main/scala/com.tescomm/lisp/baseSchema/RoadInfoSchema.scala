package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取路测数据字段信息
 * Created by wangxy on 16-1-8.
 */
object RoadInfoSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("LC.SEPRATOR", "")

  //  路测字段顺序
  val lc_length = prop.getOrElse("lc_length", "100").toInt
  val lcSampling_index = prop.getOrElse("lcSampling_index", "100").toInt
  val lclon_index = prop.getOrElse("lclon_index", "100").toInt
  val lclat_index = prop.getOrElse("lclat_index", "100").toInt
  val lclac_index = prop.getOrElse("lclac_index", "100").toInt
  val lcci_index = prop.getOrElse("lcci_index", "100").toInt
  val lcpci_index = prop.getOrElse("lcpci_index", "100").toInt
  val lcfreq_index = prop.getOrElse("lcfreq_index", "100").toInt
  val lcta_index = prop.getOrElse("lcta_index", "100").toInt
  val lcrsrp_index = prop.getOrElse("lcrsrp_index", "100").toInt
  val lcismcell_index = prop.getOrElse("lcismcell_index", "100").toInt
}
