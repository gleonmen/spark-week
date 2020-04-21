package com.endava

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object EmergencyCall {

  def main(args: Array[String]): Unit = {


    
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark = SparkSession.builder()
      .appName("Spark SQL basic example")
      .config("spark.master", "local[*]")
      .config("spark.sql.warehouse.dir", "file:///C:/tmp")
      .getOrCreate()

    val callsDataFrame = spark.read.format("csv")
      .option("header", "true")
      .option("charset", "ISO-8859-1")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .load("C:/Presentation/SPARK/example/call-123.csv")

    println ( "esquema " + callsDataFrame.schema)
    callsDataFrame.createOrReplaceTempView("emergency_line_calls")
    
    spark.sql(
      """SELECT TRIM(TIPO_INCIDENTE), COUNT (*) AS TOTAL
         FROM emergency_line_calls
         GROUP BY TRIM(TIPO_INCIDENTE)
         ORDER BY TOTAL DESC
         LIMIT 20"""
    ).show()

    spark.sql(
      """SELECT CODIGO_LOCALIDAD, TRIM(LOCALIDAD), COUNT (*) AS TOTAL
         FROM emergency_line_calls
         GROUP BY CODIGO_LOCALIDAD,  TRIM(LOCALIDAD)
         ORDER BY TOTAL DESC"""
    ).show()
    
    spark.sql(
      """SELECT  TRIM(LOCALIDAD), TRIM(TIPO_INCIDENTE), COUNT (TIPO_INCIDENTE) AS TOTAL
         FROM emergency_line_calls
         GROUP BY TRIM(TIPO_INCIDENTE),TRIM(LOCALIDAD)  
         ORDER BY TOTAL DESC, TRIM(TIPO_INCIDENTE)"""
    ).show()
    spark.close()
  }

}
