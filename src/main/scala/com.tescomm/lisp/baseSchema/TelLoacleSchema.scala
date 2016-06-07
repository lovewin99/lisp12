package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * Created by wangxy on 16-3-2.
 */
object TelLoacleSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator = prop.getOrElse("TEL.SEPRATOR", "")
  val tel_length          = prop.getOrElse("tel_length", "0").toInt
  val tel_id              = prop.getOrElse("tel_id", "0").toInt
  val tel_number          = prop.getOrElse("tel_number", "0").toInt
  val tel_areaname        = prop.getOrElse("tel_areaname", "0").toInt
  val tel_type            = prop.getOrElse("tel_type", "0").toInt
  val tel_areacode        = prop.getOrElse("tel_areacode", "0").toInt
  val tel_postcode        = prop.getOrElse("tel_postcode", "0").toInt

}
