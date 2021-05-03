package ru.zkerriga.investment.entities.openapi


case class Stock(
  figi: String,
  ticker: String,
  isin: String,
  minPriceIncrement: Option[Double],
  lot: Int,
  minQuantity: Option[Int],
  currency: String,
  name: String
)
