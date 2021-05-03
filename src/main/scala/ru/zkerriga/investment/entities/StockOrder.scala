package ru.zkerriga.investment.entities


case class StockOrder(
  figi: String,
  stopLoss: Double,
  takeProfit: Double
)
