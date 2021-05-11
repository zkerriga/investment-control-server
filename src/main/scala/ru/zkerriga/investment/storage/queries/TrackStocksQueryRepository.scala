package ru.zkerriga.investment.storage.queries

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import ru.zkerriga.investment.storage.DIO
import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.storage.entities.TrackStock
import ru.zkerriga.investment.storage.tables.TrackStocksTable


object TrackStocksQueryRepository {
  val AllTrackStocks = TableQuery[TrackStocksTable]

  def addTrackStock(clientId: Long, order: StockOrder): DIO[Long, Effect.Write] =
    (AllTrackStocks returning AllTrackStocks.map(_.id)) +=
      TrackStock(None, clientId, order.figi, order.lots, order.stopLoss, order.takeProfit)

  def getAllTrackedStocks: DIO[Seq[TrackStock], Effect.Read] =
    AllTrackStocks
      .filter(_.active)
      .result

  def markStockUntracked(stockId: Long): DIO[Int, Effect.Write] =
    AllTrackStocks
      .filter(_.id === stockId)
      .map(_.active)
      .update(false)
}
