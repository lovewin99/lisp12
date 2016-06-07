package com.tescomm.utils

import org.apache.spark.rdd.RDD
import redis.clients.jedis.{Pipeline, Jedis, JedisPool, JedisPoolConfig}
import scala.collection.JavaConversions._

/**
 * redis的操作方法
 * Created by wangxy on 15-6-11.
 */
object RedisUtils {
  val propFile = "/config/redis.properties"
  val prop = ConfigUtils.getConfig(propFile)
  val host = prop.getOrElse("REDIS.HOST", "127.0.0.1")
  val port = prop.getOrElse("REDIS.PORT", "6379").toInt

  val config: JedisPoolConfig = new JedisPoolConfig
  config.setMaxActive(200)
  config.setMaxIdle(100)
  config.setMaxWait(10000)
  config.setTestOnBorrow(true)

  var pool: JedisPool = null

  def initPool() {
    //    println(s"host=$host  port=$port")
    pool = new JedisPool(config, host, port)
  }

  def getJedis: Jedis = {
    pool.getResource
  }

  def close(pool: JedisPool, r: Jedis) = {
    if (r != null)
      pool.returnResourceObject(r)
  }

  def withConnection[A](block: Jedis => Unit) = {
    implicit var redis = this.getJedis
    try {
      block(redis)
    } catch {
      case e: Exception => System.err.println(e) //should use log in production
      //      case _ => //never should happen
    } finally {
      this.close(pool, redis)
    }
  }

  def destroyPool() {
    pool.destroy()
  }

  /**
   * 从redis取数据
   * @param tableName 表名
   * @return 返回表数据key value对集合 (不可变map)
   */
  def getResultMap(tableName: String): scala.collection.immutable.Map[String, String] = {
    initPool()
    val redis = this.getJedis
    try {
      val m = redis.hgetAll(tableName)
      m.toMap
    } catch {
      case e: Exception => System.err.println(e); scala.collection.immutable.Map[String, String]() //should use log in production
      //      case _ => //never should happen
    } finally {
      this.close(pool, redis)
      destroyPool()
    }
  }

  /**
   *  向redis中添加数据
   * @param tableName 表名
   * @param map 不可变map
   */
  def putMap2RedisTable(tableName: String, map: Map[String, String]): Unit = {
    initPool()

//    val j: Jedis = getJedis
    withConnection { j =>
      val pipe: Pipeline = j.pipelined

      map.foreach(x => {
        pipe.hset(tableName, x._1, x._2)
      })
      pipe.sync()
    }

    destroyPool()
  }

  /**
   * 向redis中添加数据
   * @param tableName redis中表名
   * @param rdd 需要处理数据的rdd
   */
  def putData2RedisTable(tableName: String, rdd: RDD[(String, String)]): Unit = {
    rdd.foreachPartition { iter =>
      initPool()

//      val j = getJedis
      withConnection { j =>
        val pipe = j.pipelined()
        iter.foreach { case (k, v) =>
          pipe.hset(tableName, k, v)
        }
        pipe.sync()
      }
      destroyPool()
    }
  }

  /**
   * 删除表
   * @param tableName 表名字
   */
  def deltable(tableName: String): Unit = {
    initPool()
//    val j: Jedis = getJedis
    withConnection { j =>
      //      val start1: Long = System.currentTimeMillis
      val pipe: Pipeline = j.pipelined

      pipe.del(tableName)
      pipe.sync()
    }
    destroyPool()
  }

  def getValueFromRedis(map: Map[String, String], key: String) = {
    map.getOrElse(key, "")
  }


  /**
   * 从redis中读取自定义value列
   * @param tableName redis中的表名
   * @param columns 自定义列名
   * @return Map(460-00-101769-11 -> 38500,11)
   */
  def getCustomerMap(tableName: String, columns: String*) = {
    val prop = ConfigUtils.getConfig("/config/initredis.properties")
    val valueSep = prop.getOrElse(s"$tableName.valuelink", ",")
    val tableColumns = prop.getOrElse(s"$tableName.value", "").split(",")
    RedisUtils.getResultMap(tableName).map { case (k, v) =>
      val lines = v.split(valueSep)
      k -> columns.map(tableColumns.indexOf(_)).map(lines(_)).mkString(valueSep)
    }

  }

  def main(args: Array[String]) {
    val map = Map("1" -> "1,2,3,4,5,6,7,8,9,10", "2" -> "10,9,8,7,6,5,4,3,2,1")
    val retMap = getCustomerMap("gclib", "tac", "ci")
    println(map, retMap)
  }
}
