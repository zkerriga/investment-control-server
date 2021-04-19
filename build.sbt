name := "investment-control-server"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("com.zkerriga.server")

lazy val AkkaVersion = "2.6.8"
lazy val AkkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,

  // slick
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.h2database" % "h2" % "1.4.200",

  // monix
  "io.monix" %% "monix" % "3.3.0",

  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
)
