package ru.zkerriga.investment.configuration


case class Configuration(
  server: ServerConf,
  database: DatabaseConf,
  tinkoff: TinkoffConf,
)
