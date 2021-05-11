package ru.zkerriga.investment.configuration

import ru.zkerriga.investment.entities.openapi.SandboxSetCurrencyBalanceRequest


case class Port(number: Int) extends AnyVal

case class ServerConf(
  host: String,
  port: Port,
  useHttps: Boolean,
)

object ServerConf {
  def getUri(c: ServerConf): String =
    s"http${if (c.useHttps) "s" else ""}://${c.host}:${c.port.number}"
}

case class DatabaseConf(
  url: String,
  user: String,
  password: String,
  driver: String,
  maxThreadPool: Option[Int],
)

case class TinkoffConf(
  url: String,
  startBalance: SandboxSetCurrencyBalanceRequest,
  token: String,
)

case class Configuration(
  server: ServerConf,
  database: DatabaseConf,
  tinkoff: TinkoffConf,
)
