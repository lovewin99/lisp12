package com.tescomm.utils

import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by s on 15-12-28.
 */
object TestUtils {


  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("just a test").setMaster("spark://cloud138:7077")
    val sc = new SparkContext(conf)

    val rdd = sc.textFile("baotest/jdbctest")
    JDBCUtils.update("oracle", rdd)

    println("--------------------------------------------------")
    println("I'm very happy!")
    println("--------------------------------------------------")

  }
}
