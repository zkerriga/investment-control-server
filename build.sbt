name := "investment-control-server"

version := "0.2"

scalaVersion := "2.13.5"

lazy val AkkaVersion = "2.6.13"
lazy val AkkaHttpVersion = "10.2.4"
lazy val circeVersion = "0.14.0-M5"
lazy val tapirVersion = "0.18.0-M4"
lazy val slickVersion = "3.3.3"

libraryDependencies ++= Seq(
  // tapir
  "com.softwaremill.sttp.tapir" % "tapir-akka-http-server_2.13" % tapirVersion,
  "com.softwaremill.sttp.tapir" % "tapir-json-circe_2.13" % tapirVersion,
  "com.softwaremill.sttp.tapir" % "tapir-swagger-ui-akka-http_2.13" % tapirVersion,
  "com.softwaremill.sttp.tapir" % "tapir-openapi-docs_2.13" % tapirVersion,
  "com.softwaremill.sttp.tapir" % "tapir-openapi-circe-yaml_2.13" % tapirVersion,

  // circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "de.heikoseeberger" % "akka-http-circe_2.13" % "1.36.0",

  // akka
  "com.typesafe.akka" % "akka-actor-typed_2.13" % AkkaVersion,
  "com.typesafe.akka" % "akka-stream_2.13" % AkkaVersion,
  "com.typesafe.akka" % "akka-http_2.13" % AkkaHttpVersion,

  // slick
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" % "slick-hikaricp_2.13" % slickVersion,
  "com.h2database" % "h2" % "1.4.200",

  // monix
  "io.monix" % "monix_2.13" % "3.3.0",

  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",

  // encryption
  "com.github.t3hnar" % "scala-bcrypt_2.13" % "4.3.0",

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.15.0",

  "org.flywaydb" % "flyway-core" % "6.2.1",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42", //org.postgresql.ds.PGSimpleDataSource dependency

  "org.scalatest" %% "scalatest" % "3.2.7" % "test",
  "org.scalamock" % "scalamock_2.13" % "4.4.0" % "test",
  "com.typesafe.akka" % "akka-stream-testkit_2.13" % AkkaVersion % "test",
  "com.typesafe.akka" % "akka-http-testkit_2.13" % AkkaHttpVersion % "test",
)
