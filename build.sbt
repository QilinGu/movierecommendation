name := """actiontest"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs, 
  "mysql" % "mysql-connector-java" % "5.1.27"
)

libraryDependencies += "org.webjars" % "bootstrap" % "3.0.2"

fork in run := true

fork in run := true