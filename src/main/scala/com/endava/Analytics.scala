package com.endava

import org.apache.log4j._
import org.apache.spark.sql._

object DataFrames {

  case class Person(ID:Int, name:String, age:Int, numFriends:Int, generatedDate:String)

  def mapper(line:String): Person = {
    val fields = line.split(',')

    val person:Person = Person(fields(0).toInt, fields(1), fields(2).toInt, fields(3).toInt,fields(4))
    return person
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]")
      //.config("spark.sql.warehouse.dir", "file:///C:/temp") // Necessary to work around a Windows bug in Spark 2.0.0; omit if you're not on Windows.
      .getOrCreate()

    import spark.implicits._
    val lines = spark.sparkContext.textFile("../fakefriends.csv")
    val people = lines.map(mapper).toDS().cache()

    val lines2 = spark.sparkContext.textFile("../fakefriends2.csv")
    val people2 = lines2.map(mapper).toDS().cache()

    val lines3 = spark.sparkContext.textFile("../fakefriends3.csv")
    val people3 = lines3.map(mapper).toDS().cache()

    /*--filtering data--*/

    import org.apache.spark.sql.functions.{col, column}

   /* println("Filter out anyone over 21:")
    people.filter(people("age") < 21).show(50)
    people.where("age < 21").show(2)
    people.where(col("age") > 21).where(col("name") =!= "Will").show(10)
    people.where(col("age") > 21).where(col("name") =!= "Will").where(col("numFriends")>2).show(10)
*/

    import org.apache.spark.sql.functions.{datediff, date_sub,to_date,lit,months_between}


   /* people.withColumn("week_ago", date_sub(col("generatedDate"), 7))
    .select(datediff(col("week_ago"), col("generatedDate"))).show()

    people.select(
      to_date(lit("2016-01-01")).alias("start"),
      to_date(col("generatedDate")).alias("end"))
      .select(months_between( col("end"),col("start"))).show()*/

    /*===================Working with differents types of data=======================

   */

    //------booleans-----------
   people.where(col("age").equalTo(30)).show(5,true)
   people.where("age==30").show(5,false)

   val ageFilter  = col("age") > 30
   val nameFilter = col("name").contains("ill")
   people.where("numFriends > 20").where(ageFilter.or(nameFilter)).show()

   val agedFilteryoung = col("age") >20
   val agedFilterOld = col("age")<30
   val numFriendsFilter = col("numFriends") > 30

   val peopleDF = people.toDF()
   peopleDF.withColumn("isYoungAndPopular",agedFilteryoung.and(agedFilterOld.and(numFriendsFilter))).
     where("isYoungAndPopular").select("name","isYoungAndPopular").show()

    //------Numbers-----------//
    import org.apache.spark.sql.functions.{expr, pow}

    val fabricatedQuantity = pow(col("age") * col("numFriends"), 2) + 5
    people.select(expr("id"), fabricatedQuantity.alias("realQuantity")).show()
    people.selectExpr("id", "(POWER((age * numFriends), 2.0) + 5) as realQuantity").show()

    import org.apache.spark.sql.functions.{corr}

    people.stat.corr("age", "numFriends")
    people.select(corr("age", "numFriends")).show()

    people.stat.crosstab("age", "numFriends").show()

    //------Strings-----------//

    import org.apache.spark.sql.functions.{initcap}

    people.select(initcap(col("name"))).show(10, false)

    import org.apache.spark.sql.functions.{lower, upper}

    people.select(col("name"),
        lower(col("name")),
        upper(lower(col("name")))).show()

    import org.apache.spark.sql.functions.regexp_replace
    val regexString = "Will|Jadzia|Weyoun"
    people.select(
      regexp_replace(col("name"), regexString, "NULL").alias("name_alter"),
      col("name")).show()

    /*-------dates---------*/

    import org.apache.spark.sql.functions.{current_date, current_timestamp}

    import org.apache.spark.sql.functions.{date_add, date_sub,to_date}
    people.select( col("generatedDate"),    date_sub(    to_date(col("generatedDate"))    , 5)     , date_add(col("generatedDate"), 5)).show()

    import org.apache.spark.sql.functions.{datediff, months_between, to_date,lit}
    people.withColumn("week_ago", date_sub(col("generatedDate"), 7)).select(datediff(col("week_ago"), col("generatedDate"))).show()

    people.select(
      to_date(lit("2016-01-01")).alias("start"),
      to_date(col("generatedDate")).alias("end"))
      .select(months_between( col("end"),col("start"))).show()

    /*===================Agregations=======================
  Aggregating is the act of collecting something together and is a cornerstone of big data analytics. In an
  aggregation, you will specify a key or grouping and an aggregation function that specifies how you
  should transform one or more columns.*/

    import org.apache.spark.sql.functions.count

    people.select(count("id")).show()

        import org.apache.spark.sql.functions.countDistinct
        people.select(countDistinct("age")).show()

        import org.apache.spark.sql.functions.{min, max}
        people.select(min("numFriends"), max("numFriends")).show()

        import org.apache.spark.sql.functions.sum
        people.select(sum("age")).show()

        import org.apache.spark.sql.functions.{sum, count, avg, expr}

        //---------average-------------

        people.select(
          count("age").alias("total_ages"),
          sum("age").alias("sum_ages"),
          avg("age").alias("avg_ages"),
          expr("mean(age)").alias("mean_ages"))
          .selectExpr(
            "sum_ages/total_ages",
            "avg_ages",
            "mean_ages").show()

    /*===================joins=======================
    A join brings together two sets of data, the left and the right, by comparing the value of one or more
    keys of the left and right and evaluating the result of a join expression that determines whether Spark
    should bring together the left set of data with the right set of data.
   */

    val joinExpression = people.col("id") === people2.col("id")

    val joinExpression2 = people.col("id") === people3.col("id")

    people.join(people2,joinExpression,"inner").orderBy(people.col("id")).show()

    people.join(people3,joinExpression2,"outer").orderBy(people.col("id"))show(50)

    people.join(people3,joinExpression2,"left_outer").orderBy(people.col("id"))show(50)

    people.join(people3,joinExpression2,"right_outer").orderBy(people.col("id"))show(50)



  }

}