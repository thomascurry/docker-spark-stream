

package com.tcurry.sparkstreaming

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import org.apache.log4j.Level
import org.apache.spark.sql.SQLContext
import org.elasticsearch.spark.sql._

import scala.util.{Try, Success, Failure}
import com.qq.spark.Utilities._

case class Data(hashtag: Array[String], location: Option[Location], place: Option[String])
case class Location(lat: Double, lon: Double)

object TwitterStreamAPI {

  def main(args: Array[String]) {

    setupTwitter()

    val ssc = new StreamingContext("local[*]", "PrintTweets", Seconds(1))
    val esConf = Map("es.nodes" -> "0.0.0.0", "es.port" -> "9200", "es.nodes.wan.only" -> "true")
    setupLogging()

    val tweets = TwitterUtils.createStream(ssc, None)
    val data = tweets.map { status =>
      val hashtags = status.getHashtagEntities.map(_.getText).map(text => s"#$text")
      val geo = status.getGeoLocation
      val geoData = Try(Option(Location(geo.getLatitude, geo.getLongitude))).toOption.flatten
      val place = Try(Option(status.getPlace.getFullName)).toOption.flatten
      (hashtags, geoData, place)
    }
    data.foreachRDD{ rdd =>
      if (!rdd.isEmpty()) {
        val filteredHashtags = rdd.filter(x => x._1.nonEmpty)
        val filteredGeo = filteredHashtags.filter(x => x._2.isDefined)
        val sqlContext = new SQLContext(rdd.sparkContext)
        import sqlContext.implicits._

        val df = filteredGeo.map(x => Data(x._1, x._2, x._3)).toDF()
        df.createOrReplaceTempView("tweets")
        df.show()

        df.saveToEs("tweets/hashgeo", esConf)

      }
    }
    // Kick it all off
    ssc.checkpoint("~/code/spark-course/streaming-course/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }
}