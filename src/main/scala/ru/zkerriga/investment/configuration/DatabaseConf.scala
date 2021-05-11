package ru.zkerriga.investment.configuration


case class DatabaseConf(
  url: String,
  user: String,
  password: String,
  driver: String,
  maxThreadPool: Option[Int],
)
