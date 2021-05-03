package ru.zkerriga.investment.storage.entities


case class Client(
  id: Option[Long],
  login: String,
  passwordHash: String,
  token: Option[String],
  active: Boolean = true
)
