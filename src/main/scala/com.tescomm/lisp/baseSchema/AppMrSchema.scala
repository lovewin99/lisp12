package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取采集层输出的appmr关联表字段信息
 * Created by wangxy on 16-1-8.
 */
object AppMrSchema {
  val propFile = "/config/segmentSeq.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val seprator        = prop.getOrElse("appmr.SEPRATOR", "")
  val appmr_length    = prop.getOrElse("appmr_length", "100").toInt
  val appmr_starttime = prop.getOrElse("appmr_starttime", "100").toInt
  val appmr_endtime   = prop.getOrElse("appmr_endtime ", "100").toInt
  val appmr_imsi      = prop.getOrElse("appmr_imsi", "100").toInt
  val appmr_msisdn    = prop.getOrElse("appmr_msisdn", "100").toInt
  val appmr_imei      = prop.getOrElse("appmr_imei", "100").toInt
  val appmr_tac       = prop.getOrElse("appmr_tac", "100").toInt
  val appmr_eci       = prop.getOrElse("appmr_eci", "100").toInt

  val appmr_enbid     = prop.getOrElse("appmr_enbid", "100").toInt
  val appmr_cellid    = prop.getOrElse("appmr_cellid", "100").toInt
  val appmr_cotype    = prop.getOrElse("appmr_cotype", "100").toInt
  val appmr_lon       = prop.getOrElse("appmr_lon", "100").toInt
  val appmr_lat       = prop.getOrElse("appmr_lat", "100").toInt
  val appmr_ta        = prop.getOrElse("appmr_ta", "100").toInt
  val appmr_sfreq     = prop.getOrElse("appmr_sfreq", "100").toInt
  val appmr_spci      = prop.getOrElse("appmr_spci", "100").toInt
  val appmr_srsrp     = prop.getOrElse("appmr_srsrp", "100").toInt
  val appmr_neinumber = prop.getOrElse("appmr_neinumber", "100").toInt

  val appmr_neilength = prop.getOrElse("appmr_neilength", "100").toInt
  val appmr_neipci    = prop.getOrElse("appmr_neipci", "100").toInt
  val appmr_neifreq   = prop.getOrElse("appmr_neifreq", "100").toInt
  val appmr_neirsrp   = prop.getOrElse("appmr_neirsrp", "100").toInt
}
