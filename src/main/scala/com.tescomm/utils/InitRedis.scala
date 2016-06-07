package com.tescomm.utils

import scala.io.Source

/**
 * Created by zcq on 15-12-28.
 * spark-submit --master spark://cloud138:7077  --driver-class-path redisclient_2.10-2.12.jar:commons-pool-1.5.6.jar --class cn.com.gis.demo.InitRedis lisp_2.10-1.0.jar
 * spark-submit --master spark://cloud138:7077  --driver-class-path redisclient_2.10-2.12.jar:commons-pool-1.5.6.jar --class cn.com.gis.demo.InitRedis lisp_2.10-1.0.jar t1 /home/tescomm/zcq/a.csv
 * spark-submit --master spark://cloud138:7077  --driver-class-path redisclient_2.10-2.12.jar:commons-pool-1.5.6.jar --class cn.com.gis.demo.InitRedis lisp_2.10-1.0.jar t2
 */
object InitRedis {
  /**
   * 没有参数则初始化配置文件中的所有表
   * 有一个参数则初始化该参数指定的表
   * 有2个参数则初始化第一个参数指定的表，且其数据在第二个参数指定的路径
   * @param args 传入的变量
   */
  def main(args: Array[String]) {
    //val (insep,cols,key,value,keylink,valuelink,location)
    //initTable("t1","/home/zcq/b.csv","/home/zcq/c.csv")
    if (args.length == 0) initTabs()
    else {
      val Array(x, y@_*) = args
      initTable(x, y: _*)
    }
  }

  /**
   * 初始化给定表
   * @param tabName:需要被初始化的表
   * @return 返回初始化后的该表的基本信息
   */
  def init(tabName: String) = {
    val prop = ConfigUtils.getConfig("/config/initredis.properties")
    val insep = prop.getOrElse(tabName + ".insep", "")
    val cols = prop.getOrElse(tabName + ".cols", "")
    val key = prop.getOrElse(tabName + ".key", "")
    val value = prop.getOrElse(tabName + ".value", "")
    val keylink = prop.getOrElse(tabName + ".keylink", "")
    val valuelink = prop.getOrElse(tabName + ".valuelink", "")
    val location = prop.getOrElse(tabName + ".location", "")
    (insep, cols, key, value, keylink, valuelink, location)
  }


  /**
   * 根据配置文件信息初始化redis中该表
   * @param tabName:redis中需要被初始化的表名
   * @param locPath:给定表名的数据存放路径
   */
  def initTable(tabName: String, locPath: String*) {
    val (insep, cols, key, value, keylink, valuelink, locationProp) = init(tabName)
    //    print(s"$locPath")
    val location = if (locPath.isEmpty) locationProp else locPath.head
    if (insep == "" || cols == "" || key == "" || value == "" || keylink == "" || valuelink == "" || location == "") {
      System.err.print("The property is not complete!")
      System.exit(1)
    }
    val iter = Source.fromFile(location, "UTF-8").getLines()
    val colArr = cols.split(",").toList
    val keyArr = key.split(",").map(colArr.indexOf(_))
    val valArr = value.split(",").map(colArr.indexOf(_))

    val mapToRedis = scala.collection.mutable.Map[String, String]()
    for (line <- iter) {
      val dataArr = line.split(insep)
      val keyStr = keyArr.map(dataArr(_)).mkString(keylink)
      val valStr = valArr.map(dataArr(_)).mkString(valuelink)
      mapToRedis += (keyStr -> valStr)
    }
    //println(mapToRedis)
    RedisUtils.deltable(tabName)
    RedisUtils.putMap2RedisTable(tabName, mapToRedis.toMap)
    println(s"already insert ${mapToRedis.size} records to $tabName")
  }

  /**
   * 初始化配置文件中所定义的所有表
   */
  def initTabs() {
    val prop = ConfigUtils.getConfig("/config/initredis.properties")
    val tabs = prop.getOrElse("init.tables", "")
    if (tabs == "") {
      println("No tables need to be initialized!")
    } else {
      tabs.split(",").foreach(initTable(_))
    }
  }

}
