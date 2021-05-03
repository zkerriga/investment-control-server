package ru.zkerriga.investment.storage.entities


case class Notification(
  id: Option[Long],
  clientId: Long,
  message: String,
  sent: Boolean = false
)
