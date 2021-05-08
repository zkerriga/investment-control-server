package ru.zkerriga.investment.entities.openapi


case class OrderBook(
  figi: String,
  depth: Int,
  bids: Seq[OrderResponse],
  asks: Seq[OrderResponse],
  lastPrice: Option[Double]
)
