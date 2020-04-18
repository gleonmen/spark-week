package com.endava

import org.apache.log4j._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.desc

object StructuredAPI extends Serializable{

  case class Person(ID: Int, name: String, age: Int, numFriends: Int, generatedDate: String)

  def mapper(line: String): Person = {
    val fields = line.split(',')
    val person: Person = Person(fields(0).toInt, fields(1), fields(2).toInt, fields(3).toInt, fields(4))
    return person
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]") //remove for a productive environment
      .config("spark.sql.warehouse.dir", "file:///C:/temp")
      .getOrCreate()

    import spark.implicits._
    val lines = spark.sparkContext.textFile("C:/Presentation/SPARK/example/fakefriends.csv")
    //RDD
    val people = lines.map(mapper).toDS().cache()

    people
      //transformations
      .groupBy("age")
      .sum("numFriends")
      .withColumnRenamed("sum(numFriends)", "total_num_friends")
      .sort(desc("total_num_friends"))
      .limit(5)
      //action
      .show()

    people.createOrReplaceTempView("friends")
    val maxSql = spark.sql(
      """SELECT age, sum(numFriends) as total_num_friends
         FROM friends
         GROUP BY age
         ORDER BY sum(numFriends) DESC
         LIMIT 5"""
    )
    maxSql.show()
 
  }
  
}
