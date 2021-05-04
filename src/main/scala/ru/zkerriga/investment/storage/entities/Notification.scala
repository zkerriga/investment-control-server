package ru.zkerriga.investment.storage.entities


case class Notification(
  id: Option[Long] = None,
  clientId: Long,
  trackStockId: Long,
  sold: Boolean = true,
  sent: Boolean = false
)
