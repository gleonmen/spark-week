package com.endava
import org.apache.spark._
import org.apache.log4j._

object LowLevelAPI extends Serializable {
  
  def mapper(line: String) = {
    val fields = line.split(',')
    (fields(2).toInt,fields(3).toInt)
  }
  
  def main(args: Array[String]): Unit = {
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "MostPopularSuperhero")
    
    // RDD
    val names = sc.textFile("C:/Presentation/SPARK/example/fakefriends.csv")
    //Transformations
    val agesRDD = names.map(mapper)
    val ageFriendsRDD = agesRDD.reduceByKey((x,y) => x + y ).cache()
    val friendsAgeRDD = ageFriendsRDD.map(x => (x._2, x._1)).sortByKey(false)
    //action
    val result = friendsAgeRDD.collect()
    
    //output
    result.take(5).foreach(x => println(s"|Age  ${x._2} | Friends ${x._1} |"))
  }
}
