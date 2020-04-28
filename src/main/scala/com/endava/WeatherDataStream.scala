package com.endava

import com.endava.data.Weather.RawWeatherData
import kafka.serializer.StringDecoder
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WeatherDataStream {
  val localLogger = Logger.getLogger("WeatherDataStream")
  def main(args: Array[String]) {

    // update
    // val checkpointDir = "./tmp"

    val sparkConf = new SparkConf().setAppName("Raw Weather")
    sparkConf.setIfMissing("spark.master", "local[5]")
    //    sparkConf.setIfMissing("spark.checkpoint.dir", checkpointDir)
    sparkConf.setIfMissing("spark.cassandra.connection.host", "127.0.0.1")

    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val kafkaTopicRaw = "raw_weather"
    val kafkaBroker = "127.0.01:9092"

    val cassandraKeyspace = "isd_weather_data"
    val cassandraTableRaw = "raw_weather_data"
    val cassandraTableDailyPrecip = "daily_aggregate_precip"

    println(s"using cassandraTableDailyPrecip $cassandraTableDailyPrecip")

    val topics: Set[String] = kafkaTopicRaw.split(",").map(_.trim).toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> kafkaBroker)

    localLogger.info(s"connecting to brokers: $kafkaBroker")
    localLogger.info(s"kafkaParams: $kafkaParams")
    localLogger.info(s"topics: $topics")

    val rawWeatherStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    val parsedWeatherStream: DStream[RawWeatherData] = ingestStream(rawWeatherStream)

    persist(cassandraKeyspace, cassandraTableRaw, cassandraTableDailyPrecip, parsedWeatherStream)

    parsedWeatherStream.print // for demo purposes only

    //Kick off
    ssc.start()

    ssc.awaitTermination()

    ssc.stop()
  }

  
  def persist(CassandraKeyspace: String, CassandraTableRaw: String,
              CassandraTableDailyPrecip: String,
              parsedWeatherStream: DStream[RawWeatherData]): Unit = {

    import com.datastax.spark.connector.streaming._

    /** Saves the raw data to Cassandra - raw table. */
    parsedWeatherStream.saveToCassandra(CassandraKeyspace, CassandraTableRaw)
    parsedWeatherStream.map { weather =>
      (weather.wsid, weather.year, weather.month, weather.day, weather.oneHourPrecip)
    }.saveToCassandra(CassandraKeyspace, CassandraTableDailyPrecip)
  }
  
  def ingestStream(rawWeatherStream: InputDStream[(String, String)]): DStream[RawWeatherData] = {
    val parsedWeatherStream = rawWeatherStream.map(_._2.split(","))
      .map(RawWeatherData(_))
    parsedWeatherStream
  }
}
