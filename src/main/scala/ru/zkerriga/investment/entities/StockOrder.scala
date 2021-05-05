package ru.zkerriga.investment.entities

import ru.zkerriga.investment.storage.entities.TrackStock


case class StockOrder(
  figi: String,
  lots: Int,
  stopLoss: Double,
  takeProfit: Double
)

object StockOrder {
  def from(trackStock: TrackStock): StockOrder =
    StockOrder(
      figi = trackStock.figi,
      lots = trackStock.lots,
      stopLoss = trackStock.stopLoss,
      takeProfit = trackStock.takeProfit
    )
}
