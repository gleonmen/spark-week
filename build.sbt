name := "spark-week"
version := "0.1-SNAPSHOT"
organization := "com.endava" // change to your org

mainClass in (Compile, run) := Some("com.endava.StructuredAPI")
assemblyJarName in assembly := "spark-week.jar"
test in assembly := {}

//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

//scalaVersion := "2.13.1"
//scalaVersion := "2.11.12"
scalaVersion := "2.10.6"

// allows us to include spark packages
resolvers += "bintray-spark-packages" at
  "https://dl.bintray.com/spark-packages/maven/"

resolvers += "Typesafe Simple Repository" at
  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

resolvers += "MavenRepository" at
  "https://mvnrepository.com/"

resolvers += "jitpack" at "https://jitpack.io"

//val sparkVersion = "2.4.5"
val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(

  "com.groupon.sparklint" %% "sparklint-spark162" % "1.0.4" excludeAll (
    ExclusionRule(organization = "org.apache.spark")
    ),
  // spark core
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  // spark-modules
  "org.apache.spark" %% "spark-graphx" % sparkVersion,
  "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % sparkVersion,
  "org.apache.spark" %% "spark-streaming_2.11" % sparkVersion,
  
  //Integration
  "org.apache.spark" %% "spark-streaming-kafka-0-8_2.11" %,
  "org.apache.kafka" % "kafka-clients" % "0.11.0.1",
"com.datastax.spark" %% "spark-cassandra-connector" % "1.6.0" % "provided",
  
  // spark packages
  "graphframes" % "graphframes" % "0.4.0-spark2.1-s_2.11",
  // testing
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
  // logging
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
)
