package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * Created by wangxy on 16-3-2.
 */
object CustomAreaSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("CA.SEPRATOR", "")
  val ca_areaid       = prop.getOrElse("ca_areaid", "0").toInt
  val ca_lon          = prop.getOrElse("ca_lon", "0").toInt
  val ca_lat          = prop.getOrElse("ca_lat", "0").toInt
  val ca_x            = prop.getOrElse("ca_x", "0").toInt
  val ca_y            = prop.getOrElse("ca_y", "0").toInt
}
