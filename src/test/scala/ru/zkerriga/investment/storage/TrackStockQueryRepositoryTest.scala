package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.storage.queries.TrackStocksQueryRepository._
import ru.zkerriga.investment.entities.StockOrder


class TrackStockQueryRepositoryTest extends DatabaseSuite with Matchers {
  test("add new stock should return id") {
    for {
      res <- addTrackStock(1, StockOrder("FIGI4", 1, 100.0, 120.0))
    } yield assert(res >= 0L)
  }

  test("get tracked stocks and mark unread") {
    for {
      seq <- getAllTrackedStocks
      _ <- markStockUntracked(1)
      seq2 <- getAllTrackedStocks
    } yield {
      seq shouldEqual sampleTrackStocks
      seq2 shouldEqual sampleTrackStocks.tail
    }
  }
}
