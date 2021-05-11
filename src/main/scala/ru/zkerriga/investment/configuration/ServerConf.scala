package ru.zkerriga.investment.configuration


case class ServerConf(
  host: String,
  port: Port,
  useHttps: Boolean,
)

object ServerConf {
  def getUri(c: ServerConf): String =
    s"http${if (c.useHttps) "s" else ""}://${c.host}:${c.port.number}"
}
