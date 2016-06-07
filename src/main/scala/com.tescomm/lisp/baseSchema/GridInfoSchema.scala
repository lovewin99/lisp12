package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取栅格基础数据字段信息
 * Created by wangxy on 16-1-6.
 */
object GridInfoSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("SG.SEPRATOR", "")
  //  栅格信息字段
  val sg_time     = prop.getOrElse("sg_time", "0").toInt
  val sg_x        = prop.getOrElse("sg_x", "0").toInt
  val sg_y        = prop.getOrElse("sg_y", "0").toInt
  val sg_lon      = prop.getOrElse("sg_lon", "0").toInt
  val sg_lat      = prop.getOrElse("sg_lat", "0").toInt
  val sg_tac      = prop.getOrElse("sg_tac", "0").toInt
  val sg_eci      = prop.getOrElse("sg_eci", "0").toInt
  val sg_area     = prop.getOrElse("sg_area", "0").toInt
  val sg_subarea  = prop.getOrElse("sg_subarea", "0").toInt
  val sg_nature   = prop.getOrElse("sg_nature", "0").toInt
  val sg_traffic  = prop.getOrElse("sg_traffic", "0").toInt
  val sg_custom   = prop.getOrElse("sg_traffic", "0").toInt
  val sg_road     = prop.getOrElse("sg_road", "0").toInt
  val sg_roadcode = prop.getOrElse("sg_roadcode", "0").toInt
  val sg_roadname = prop.getOrElse("sg_roadname", "0").toInt
  val sg_ismain = prop.getOrElse("sg_ismain", "0").toInt
}

