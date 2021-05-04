package ru.zkerriga.investment.entities


case class StockOrder(
  figi: String,
  lots: Int,
  stopLoss: Double,
  takeProfit: Double
)
