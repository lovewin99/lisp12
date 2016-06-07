package com.tescomm.lisp.baseSchema

/**
 * 读取工参表字段信息
 * Created by wangxy on 16-1-5.
 */

import com.tescomm.utils.ConfigUtils

object WorkParaSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("GC.SEPRATOR", "")

  //  工参字段顺序
  val gc_lon = prop.getOrElse("gc_lon", "100").toInt
  val gc_lat = prop.getOrElse("gc_lat", "100").toInt
  val gc_pci = prop.getOrElse("gc_pci", "100").toInt
  val gc_freq = prop.getOrElse("gc_freq", "100").toInt
  val gc_inout = prop.getOrElse("gc_inout", "100").toInt
  val gc_angle = prop.getOrElse("gc_angle", "100").toInt
  val gc_downangle = prop.getOrElse("gc_downangle", "100").toInt
  val gc_enbid = prop.getOrElse("gc_enbid", "100").toInt
  val gc_tac = prop.getOrElse("gc_tac", "100").toInt
  val gc_eci = prop.getOrElse("gc_eci", "100").toInt
}
