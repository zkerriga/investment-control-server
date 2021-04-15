name := "investment-control-server"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("com.zkerriga.server")

lazy val AkkaVersion = "2.6.8"
lazy val AkkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)
