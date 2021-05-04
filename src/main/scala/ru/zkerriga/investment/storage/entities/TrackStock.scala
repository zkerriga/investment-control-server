package ru.zkerriga.investment.storage.entities


case class TrackStock(
  id: Option[Long],
  clientId: Long,
  figi: String,
  lots: Int,
  stopLoss: Double,
  takeProfit: Double,
  active: Boolean = true
)
