package ru.zkerriga.investment.configuration

import monix.eval.Task
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures


case class Configuration(
  server: ServerConf,
  database: DatabaseConf,
  tinkoff: TinkoffConf,
)

object Configuration {
  def getConfiguration: Task[Configuration] = {
    import pureconfig.generic.auto._
    val configSource = ConfigSource.default.load[Configuration]

    Task.fromEither(
      (err: ConfigReaderFailures) => new RuntimeException(s"Invalid configuration: $err")
    )(configSource)
  }
}
