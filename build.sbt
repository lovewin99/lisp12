import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

name := "lisp12"

version := "1.0"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "maven Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "2.1.0",
  "org.apache.spark" % "spark-streaming_2.10" % "1.2.0",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.2.0",
  "net.debasishg" % "redisclient_2.10" % "2.12",
  "com.oracle" % "ojdbc14" % "10.2.0.4.0"
)

//excludeFilter in unmanagedJars := "javax.transaction-1.1.1.v201105210645.jar"
//excludeFilter in unmanagedJars := "javax.mail.glassfish-1.4.1.v201005082020.jar"
//excludeFilter in unmanagedJars := "javax.activation-1.1.0.v201105071233.jar"

//mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
//{
//  case PathList("com", "esotericsoftware", "minlog", xs @ _*) => MergeStrategy.first
//  case PathList("org", "jboss","netty", xs @ _*) => MergeStrategy.last
//  case PathList("com", "google","common", xs @ _*) => MergeStrategy.last
//  case PathList("com", "google","protobuf", xs @ _*) => MergeStrategy.last
//  case PathList("javax", "xml","stream", xs @ _*) => MergeStrategy.first
//  case PathList("com", "sun","jersey", xs @ _*) => MergeStrategy.last
//  case PathList("org", "codehaus","jackson", xs @ _*) => MergeStrategy.last
//  case PathList("com", "codahale", xs @ _*)=> MergeStrategy.first
//  //  case PathList("com", "amazonaws", xs @ _*)=> MergeStrategy.first
//  case PathList("org", "json", xs @ _*) => MergeStrategy.first
//  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
//  case PathList("org", "apache", xs @ _*)=> MergeStrategy.first
//  case PathList("org", "eclipse", xs @ _*) => MergeStrategy.first
//  case PathList("akka",  xs @ _*)=> MergeStrategy.first
//  case PathList("parquet",  xs @ _*)=> MergeStrategy.first
//  case PathList("jodd",  xs @ _*)=> MergeStrategy.last
//  case PathList("META-INF", xs @ _*) =>
//    (xs map {_.toLowerCase}) match {
//      case ("eclipsef.rsa" :: Nil) => MergeStrategy.discard
//    }
//      case PathList("au", "com", "bytecode", "opencsv",  xs @ _*)=> MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith "jboss-beans.xml" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "pom.properties" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "pom.xml" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "overview.html" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "plugin.xml" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "parquet.thrift" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "Log$1.class" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "Log.class" => MergeStrategy.filterDistinctLines
//  case PathList(ps @ _*) if ps.last endsWith "jersey-module-version" => MergeStrategy.filterDistinctLines
//  //  case PathList(ps @ _*) if ps.last endsWith "CSVParser.class" => MergeStrategy.last
//  //  case PathList(ps @ _*) if ps.last endsWith "DisableAlarmActionsRequest.class" => MergeStrategy.filterDistinctLines
//  case "application.conf" => MergeStrategy.concat
//  case "unwanted.txt"     => MergeStrategy.discard
//  case x => old(x)
//  case _ => MergeStrategy.deduplicate
//}
//}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { mergeStrategy => {
  case entry => {
    val strategy = mergeStrategy(entry)
    if (strategy == MergeStrategy.deduplicate) MergeStrategy.first
    else strategy
  }
}}


jarName in assembly := "lisp12.jar"