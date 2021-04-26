name := "investment-control-server"

version := "0.1"

scalaVersion := "2.13.5"

lazy val AkkaVersion = "2.6.13"
lazy val AkkaHttpVersion = "10.2.4"
lazy val circeVersion = "0.13.0"
lazy val tapirVersion = "0.18.0-M4"

libraryDependencies ++= Seq(
  // tapir
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,

  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.36.0",

  // circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

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

  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
)
