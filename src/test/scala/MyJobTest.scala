import com.endava.{DatasetUtils, Person}

class MyJobTest extends BaseSpec {
  import testImplicits._
  
  "Well formatted strings" should "just work" in {
    val authors = Seq("Dev1,endava", "Dev2,endava")
    
    val authorsRDD = spark
      .sparkContext
      .parallelize(authors)
      .map(DatasetUtils.createPersonFromString(_))

    val authorsDataset = spark.createDataFrame(authorsRDD).as[Person]

    assert(authorsDataset.count() == 2)
  }

  "Poorly formatted strings" should "not work" in {
    val authors = Seq(
      "Dev2",
      "Dev1",
      "Dev1,endava,Dev2,endava",
      "poorly formatted")

    
    val authorsDataset = spark
      .sparkContext
      .parallelize(authors)
      .map(DatasetUtils.createPersonFromString(_))
      .toDF()
      .as[Person]

    authorsDataset.show()
    assert(authorsDataset.where("name is not null").count() == 0)
  }

}
