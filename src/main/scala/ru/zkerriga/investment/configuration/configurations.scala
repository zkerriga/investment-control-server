package ru.zkerriga.investment.configuration

case class Port(number: Int) extends AnyVal

case class ServerConf(
  host: String,
  port: Port,
  useHttps: Boolean,
)

case class DatabaseConf(
  url: String,
  user: String,
  password: String,
  driver: String,
  maxThreadPool: Option[Int],
)

case class TinkoffConf(url: String, token: String)

case class Configuration(
  server: ServerConf,
  database: DatabaseConf,
  tinkoff: TinkoffConf,
)
