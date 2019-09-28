organization := "com.example"

name := "korolev-workshop"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

val korolevVersion = "0.12.1"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.+",
  "com.github.fomkin" %% "korolev-server-akkahttp" % korolevVersion,
  "org.iq80.leveldb" % "leveldb" % "0.12",
  "com.github.fomkin" %% "zhukov-derivation" % "0.3.2"
)
