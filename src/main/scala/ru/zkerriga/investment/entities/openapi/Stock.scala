package ru.zkerriga.investment.entities.openapi


case class Stock(
  figi: String,
  ticker: String,
  isin: String,
  minPriceIncrement: Double,
  lot: Int,
  minQuantity: Int,
  currency: String,
  name: String
)
