package com.tescomm.lisp.baseSchema

import com.tescomm.utils.ConfigUtils

/**
 * 读取指纹定位算法配置信息
 * Created by wangxy on 16-1-6.
 */
object FingerCfgSchema {
  val propFile = "/config/fingerConfig.properties"
  val prop = ConfigUtils.getConfig(propFile)

  val finger_line_max_num = prop.getOrElse("FINGER_LINE_MAX_NUM", "12").toInt
  val data_per_grid = prop.getOrElse("DATA_PER_GIRD", "6").toInt
  val rsrp_down_border = prop.getOrElse("RSRP_DOWN_BORDER", "-100").toInt
  val rssi_uplimit = prop.getOrElse("RSSI_UPLIMIT", "-40").toInt
  val rssi_downlimit = prop.getOrElse("RSSI_DOWNLIMIT", "-100").toInt
  val bseq_index = prop.getOrElse("SEQ_INDEX", "5").toInt
  val bdiff_value = prop.getOrElse("DIFF_VALUE", "12").toInt
  //  val bmaxdiff_value = prop.getOrElse("MAXDIFF_VALUE", "97").toInt
  val isfilter_by_mcell = prop.getOrElse("ISFILTER_BY_MCELL", "1")
  val filterByDistance_percent = prop.getOrElse("FILTERBYDISTANCE_PERCENT", "0").toFloat
  //1:方差 2:绝对平均差 3:相关系数
  val calculate_choice = prop.getOrElse("CALCULATE_CHOICE", "1").toInt
  val samefactor_limit = prop.getOrElse("SAMEFACTOR_LIMIT", "1.0").toFloat
  val variance_limit = prop.getOrElse("VARIANCE_LIMIT", "99999999").toInt
  val averdiff_limit = prop.getOrElse("AVERDIFF_LIMIT", "97").toInt
  val similar_percent = prop.getOrElse("SIMILAR_PERCENT", "0.0").toFloat
  val istwice_compare = prop.getOrElse("ISTWICE_COMPARE", "0").toInt
  val twicedistance_limit = prop.getOrElse("TWICEDISTANCE_LIMIT", "100.0").toFloat
  val twicetime_limit = prop.getOrElse("TWICETIME_LIMIT", "3000.0").toLong
  val variance_offset = prop.getOrElse("VARIANCE_OFFSET", "100").toDouble
  val averdiff_offset = prop.getOrElse("AVERDIFF_OFFSET", "30").toDouble

  val combineinfo_num = prop.getOrElse("COMBINEINFO_NUM", "2").toInt
  val combineinfo_timelimit = prop.getOrElse("COMBINEINFO_TIMELIMIT", "20000").toInt
}
