package ru.zkerriga.investment.entities.openapi


case class Order (
  orderId: String,
  figi: String,
  operation: String,
  status: String,
  requestedLots: Int,
  executedLots: Int,
  `type`: String,
  price: Double
)
