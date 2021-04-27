package ru.zkerriga.investment

trait ServerConfiguration {
  val interface: String = "localhost"
  val port: Int = 8080
  val api = "/api/v1/investment"
  val link = s"http://$interface:$port$api"
}
