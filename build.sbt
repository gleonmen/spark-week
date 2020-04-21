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
scalaVersion := "2.11.12"

// allows us to include spark packages
resolvers += "bintray-spark-packages" at
  "https://dl.bintray.com/spark-packages/maven/"

resolvers += "Typesafe Simple Repository" at
  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

resolvers += "MavenRepository" at
  "https://mvnrepository.com/"


//val sparkVersion = "2.4.5"
val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  // spark core
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion,
  // spark-modules
  "org.apache.spark" %% "spark-graphx" % sparkVersion,
  // "org.apache.spark" %% "spark-mllib" % sparkVersion,

  // spark packages
  "graphframes" % "graphframes" % "0.4.0-spark2.1-s_2.11",

  // testing
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",

  // logging
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.6.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
)