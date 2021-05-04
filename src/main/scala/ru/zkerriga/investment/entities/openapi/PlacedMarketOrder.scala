package ru.zkerriga.investment.entities.openapi


case class PlacedMarketOrder(
  orderId: String,
  operation: String,
  status: String,
  rejectReason: Option[String],
  message: Option[String],
  requestedLots: Int,
  executedLots: Int
)
