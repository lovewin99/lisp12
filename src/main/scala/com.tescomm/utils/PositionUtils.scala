package com.tescomm.utils

/**
 * 各地图API坐标系统比较与转换
 * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
 * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）
 * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
 * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系 BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系
 * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。
 */
object PositionUtils {
  private val PI = 3.1415926535897932384626
  private val A = 6378245.0
  private val EE = 0.00669342162296594323


  /**
   * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
   *
   * @param lat 经度
   * @param lon 纬度
   * @return
   */
  def gps84_To_Gcj02(lat: Double, lon: Double): (Double, Double) = {
    if(outOfChina(lat, lon)) return (-1.0, -1.0)
    var dLat = transformLat(lon - 105.0, lat - 35.0)
    var dLon = transformLon(lon - 105.0, lat - 35.0)
    val radLat = lat / 180.0 * PI
    var magic = Math.sin(radLat)
    magic = 1 - EE * magic * magic
    val sqrtMagic = Math.sqrt(magic)
    dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
    dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
    (lat + dLat, lon + dLon)
  }


  /**
   * * 火星坐标系 (GCJ-02) to 84
   * @param lon 经度
   * @param lat 纬度
   * @return
   * */
  def gcj_To_Gps84(lat: Double, lon:Double) = {
    val (wgLat, wgLon) = transform(lat, lon)
    (lat * 2 - wgLat, lon * 2 - wgLon)
  }


  /**
   * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
   *
   * @param gg_lat 经度
   * @param gg_lon 纬度
   */
  def gcj02_To_Bd09(gg_lat: Double, gg_lon: Double) = {
    val x = gg_lon
    val y = gg_lat
    val z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI)
    val theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI)
    val bd_lon = z * Math.cos(theta) + 0.0065
    val bd_lat = z * Math.sin(theta) + 0.006
    (bd_lat, bd_lon)
  }


  /**
   * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法
   * 将 BD-09 坐标转换成GCJ-02 坐标
   * @param bd_lat 经度
   * @param bd_lon 纬度
   * @return
   */
  def bd09_To_Gcj02(bd_lat: Double, bd_lon: Double) = {
    val x = bd_lon - 0.0065
    val y = bd_lat - 0.006
    val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI)
    val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI)
    val gg_lon = z * Math.cos(theta)
    val gg_lat = z * Math.sin(theta)
    (gg_lat, gg_lon)
  }

  /**
   * (BD-09)-->84
   * @param bd_lat 经度
   * @param bd_lon 纬度
   * @return
   */
  def bd09_To_Gps84(bd_lat: Double, bd_lon: Double) = {
    val (wgLat, wgLon) = PositionUtils.bd09_To_Gcj02(bd_lat, bd_lon)
    PositionUtils.gcj_To_Gps84(wgLat, wgLon)
  }

  def outOfChina(lat: Double, lon: Double): Boolean = {
    lon < 72.004 || lon > 137.8347 || lat < 0.8239 || lat > 55.8271
  }

  def transform(lat: Double, lon: Double): (Double, Double) = {
    if(outOfChina(lat, lon)) return (lat, lon)
    var dLat = transformLat(lon - 105.0, lat - 35.0)
    var dLon = transformLon(lon - 105.0, lat - 35.0)
    val radLat = {
      lat / 180.0 * PI
    }
    var magic = Math.sin(radLat)
    magic = 1 - EE * magic * magic
    val sqrtMagic = Math.sqrt(magic)
    dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
    dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
    (lat + dLat, lon + dLon)
  }

  def transformLat(x: Double, y: Double) = {
    var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
              + 0.2 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0
    ret
  }

  def transformLon(x: Double, y: Double) = {
    var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0
      * PI)) * 2.0 / 3.0
    ret
   }

  def main(args: Array[String]) {
    // 113.9718816, 23.11949047
    // 113.9718816, 23.11949047
    val lat = 23.11949047
    val lon = 113.9718816
    println(s"original:\t\t($lat, $lon)")
    println(s"gps84_To_Gcj02:\t${gps84_To_Gcj02(lat, lon)}")
    println(s"gcj_To_Gps84:\t${gcj_To_Gps84(lat, lon)}")
    println(s"gcj02_To_Bd09:\t${gcj02_To_Bd09(lat, lon)}")
    println(s"bd09_To_Gcj02:\t${bd09_To_Gcj02(lat, lon)}")
    println(s"bd09_To_Gps84:\t${bd09_To_Gps84(lat, lon)}")
  }
}