package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers
import TrackStocksQueryRepository._
import ru.zkerriga.investment.entities.StockOrder

class TrackStockQueryRepositoryTest extends DatabaseSuite with Matchers {
  test("add new stock should return id") {
    for {
      res <- addTrackStock(1, StockOrder("FIGI4", 100.0, 120.0))
    } yield assert(res >= 0L)
  }
}
