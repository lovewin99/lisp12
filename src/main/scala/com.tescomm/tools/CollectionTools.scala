package com.tescomm.tools

import com.tescomm.utils.DateUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by zcq on 16-1-13.
  */
object CollectionTools {
  def main(args: Array[String]) {
    val a = "zcq/"
    val ab = new ArrayBuffer[String]()
    //    val b = getInputStr(a)
    //    println(b)
    //    print("!!!!!!!!")
    //    println(getInput(a,ab))

    println(getInput(a, "2016-03-01"))
  }

  /**
    * 根据给定输入路径前缀和时间间隔获得当前时间第n天前到前一天的输入路径的数组
    *
    * @param prefix 给定的输入路径前缀
    * @param n      给定时间间隔
   * @param currDate 传入的当前时间
    * @return 返回当前时间第n天前到前一天的输入路径的数组
    */
  def getInput(prefix: String, currDate: String, n: Int = -7): ArrayBuffer[String] = {
    //val t1 = "2016-01-05"
    //val nowDate = DateUtils.getNowDate()
    if (n == (-1)) {
      //ArrayBuffer(prefix + DateUtils.getStartTime(nowDate, -1).replace("-", "") + "/*/*/*/")
      ArrayBuffer(prefix + DateUtils.getStartTime(currDate, -1).replace("-", "") + "/*/*/*/")
    } else {
      //ArrayBuffer(prefix + DateUtils.getStartTime(nowDate, n).replace("-", "") + "/*/*/*/") ++= getInput(prefix, n + 1)
      ArrayBuffer(prefix + DateUtils.getStartTime(currDate, n).replace("-", "") + "/*/*/*/") ++= getInput(prefix, currDate, n + 1)
    }
  }

  /**
    * 根据给定输入路径前缀和时间间隔获得当前时间第n天前到前一天的输入路径的数组
    *
    * @param prefix 给定的输入路径前缀
    * @param dt     给定日期
    * @return 返回当前时间第n天前到前一天的输入路径的数组
    */
  def getDayFilePath(prefix: String, dt: String) = {
    prefix + dt.replace("-", "").slice(0, 8) + "/*"
  }

  /**
    * 根据给定输入路径前缀和时间间隔获得当前时间第n天前到前一天的输入路径的数组
    *
    * @param prefix 给定的输入路径前缀
    * @param dt     给定日期
    * @return 返回当前时间第n天前到前一天的输入路径的数组
    */
  def getHourFilePath(prefix: String, dt: String) = {
    prefix + "/" + dt.slice(0, 8) + "/" + dt.slice(8, 10) + "/*"
  }

}
