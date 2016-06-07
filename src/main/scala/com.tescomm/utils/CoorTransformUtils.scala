package com.tescomm.utils

import scala.math._

/**
 * Created by zcq
 * 2015-12-31.
 */
object CoorTransformUtils {
  val aM = 6378245.0
  val ee = 0.00669342162296594323
  val x_pi = Pi * 3000.0 / 180.0
  val gridSize = ConfigUtils.getConfig("/config/global.properties").getOrElse("grid.size", "1").toDouble


  /**
   * 计算两经纬度之间的距离
   * @param lon1 地球上A点的经度
   * @param lat1 地球上A点的纬度
   * @param lon2 地球上B点的经度
   * @param lat2 地球上B点的纬度
   * @return A点和B点之间的距离
   */
  def calc_distance(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double = {
    val Rc = 6378137.00 // 赤道半径
    val Rj = 6356725 // 极半径

    val radLo1 = lon1 * Pi / 180
    val radLa1 = lat1 * Pi / 180
    val Ec1 = Rj + (Rc - Rj) * (90.0 - lat1) / 90.0
    val Ed1 = Ec1 * cos(radLa1)

    val radLo2 = lon2 * Pi / 180
    val radLa2 = lat2 * Pi / 180

    val dx = (radLo2 - radLo1) * Ed1
    val dy = (radLa2 - radLa1) * Ec1
    val dDeta = sqrt(dx * dx + dy * dy)
    dDeta
  }


  /**
   * 栅格转经纬度
   * @param x 栅格横坐标
   * @param y 栅格纵坐标
   * @return 转换后的经纬度
   */
  def grid2lonlat(x: Int, y: Int): (Double, Double) = {
    val lon = x * gridSize / 20037508.34 * 180
    var lat = y * gridSize / 20037508.34 * 180
    lat = 180 / Pi * (2 * atan(exp(lat * Pi / 180)) - Pi / 2)
    (lon, lat)
  }

  /**
   * 墨卡托转经纬度
   * @param x 墨卡托横坐标
   * @param y 墨卡托纵坐标
   * @return
   */
  def Mercator2lonlat(x: Int, y: Int): (Double, Double) = {
    val lon = x / 20037508.34 * 180
    var lat = y / 20037508.34 * 180
    lat = 180 / Pi * (2 * atan(exp(lat * Pi / 180)) - Pi / 2)
    (lon, lat)
  }

  //*******************************************************/
  // 地球坐标转火星坐标
  def outOfChina(lon: Double, lat: Double): Boolean = {
    lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271
  }

  def transformLat(x: Double, y: Double): Double = {
    var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
    ret += (20.0 * sin(6.0 * x * Pi) + 20.0 * sin(2.0 * x * Pi)) * 2.0 / 3.0
    ret += (160.0 * sin(y / 12.0 * Pi) + 320 * sin(y * Pi / 30.0)) * 2.0 / 3.0
    ret
  }

  def transformLon(x: Double, y: Double): Double = {
    var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
    ret += (20.0 * sin(6.0 * x * Pi) + 20.0 * sin(2.0 * x * Pi)) * 2.0 / 3.0
    ret += (20.0 * sin(x * Pi) + 40.0 * sin(x / 3.0 * Pi)) * 2.0 / 3.0
    ret += (150.0 * sin(x / 12.0 * Pi) + 300.0 * sin(x / 30.0 * Pi)) * 2.0 / 3.0
    ret
  }

  //也是地球坐标转火星坐标 wgs gcj
  def wgsTOgcj(wgLon: Double, wgLat: Double): (Double, Double) = {
    if (outOfChina(wgLon, wgLat)) {
      (wgLon, wgLat)
    } else {
      var dLat = transformLat(wgLon - 105.0, wgLat - 35.0)
      var dLon = transformLon(wgLon - 105.0, wgLat - 35.0)
      val radLat = wgLat / 180.0 * Pi
      var magic = sin(radLat)
      magic = 1 - ee * magic * magic
      val sqrtMagic = sqrt(magic)
      dLat = (dLat * 180.0) / ((aM * (1 - ee)) / (magic * sqrtMagic) * Pi)
      dLon = (dLon * 180.0) / (aM / sqrtMagic * cos(radLat) * Pi)
      val mgLat = wgLat + dLat
      val mgLon = wgLon + dLon
      (mgLon, mgLat)
    }
  }

  //*******************************************************/
  //经纬度转墨卡托
  def lonLat2Mercator(lon: Double, lat: Double): (Double, Double) = {
    val x = lon * 20037508.342789 / 180
    var y = log(tan((90 + lat) * Pi / 360)) / (Pi / 180)
    y = y * 20037508.34789 / 180
    (x, y)
  }


  /**
   * 地球坐标转墨卡托坐标
   * @param wgLon 地球上某点的经度
   * @param wgLat 地球上某点的纬度
   * @return 转换后的墨卡托坐标
   */
  def wgs2Mercator(wgLon: Double, wgLat: Double): (Double, Double) = {
    val (lon, lat) = outOfChina(wgLon, wgLat) match {
      case true => (wgLon, wgLat)
      case _ =>
        var dLat = transformLat(wgLon - 105.0, wgLat - 35.0)
        var dLon = transformLon(wgLon - 105.0, wgLat - 35.0)
        val radLat = wgLat / 180.0 * Pi
        var magic = sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((aM * (1 - ee)) / (magic * sqrtMagic) * Pi)
        dLon = (dLon * 180.0) / (aM / sqrtMagic * cos(radLat) * Pi)
        val mgLat = wgLat + dLat
        val mgLon = wgLon + dLon
        (mgLon, mgLat)
    }

    val x = lon * 20037508.342789 / 180
    var y = log(tan((90 + lat) * Pi / 360)) / (Pi / 180)
    y = y * 20037508.34789 / 180
    (x, y)
  }


  /**
   * 地球坐标转墨卡托坐标，再除以栅格大小，得到栅格中心点坐标
   * @param wgLon 地球上某点的经度
   * @param wgLat 地球上某点的纬度
   * @return 转换后的栅格中心点坐标
   */
  def wgs2grid(wgLon: Double, wgLat: Double) = {
    val (lon, lat) = outOfChina(wgLon, wgLat) match {
      case true => (wgLon, wgLat)
      case _ =>
        var dLat = transformLat(wgLon - 105.0, wgLat - 35.0)
        var dLon = transformLon(wgLon - 105.0, wgLat - 35.0)
        val radLat = wgLat / 180.0 * Pi
        var magic = sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((aM * (1 - ee)) / (magic * sqrtMagic) * Pi)
        dLon = (dLon * 180.0) / (aM / sqrtMagic * cos(radLat) * Pi)
        val mgLat = wgLat + dLat
        val mgLon = wgLon + dLon
        (mgLon, mgLat)
    }

    val x = lon * 20037508.342789 / 180
    var y = log(tan((90 + lat) * Pi / 360)) / (Pi / 180)
    y = y * 20037508.34789 / 180
    (Math.rint(x / gridSize).toInt, Math.rint(y / gridSize).toInt)
  }

  def lonlat2grid(lon: Double, lat: Double) = {
    val x = lon * 20037508.342789 / 180
    var y = log(tan((90 + lat) * Pi / 360)) / (Pi / 180)
    y = y * 20037508.34789 / 180
    (Math.rint(x / gridSize).toInt, Math.rint(y / gridSize).toInt)
  }


  def main(args: Array[String]) {
    val a = 120.344445
    val b = 30.879655
    val t = wgsTOgcj(a, b)
    val (lon, lat) = lonLat2Mercator(t._1, t._2)
    println(s"$lon,$lat")

    val (x, y) = wgs2Mercator(a, b)
    println(s"$x,$y")

    val (m, n) = wgs2grid(120.34567, 30.45678)
    println(s"$m,$n")

    val (m1, n1) = wgs2Mercator(104.4470000, 22.857100)
    println(s"$m1, $n1")
  }

}
