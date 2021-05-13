package ru.zkerriga.investment.monitoring

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.TestOpenApiClient
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi.{PlacedMarketOrder, TinkoffResponse}
import ru.zkerriga.investment.exceptions._
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class StocksMonitoringTest extends AnyFlatSpec with Matchers {

  import monix.execution.Scheduler.Implicits.global

  private def generateResponse[A](payload: A): TinkoffResponse[A] =
    TinkoffResponse[A]("", "Ok", payload)

  private def suite: (FakeMonitoringDao, StocksMonitoringImpl) = {
    val fakeDb = new FakeMonitoringDao
    val monitoring = new StocksMonitoringImpl(TestOpenApiClient, fakeDb, TinkoffToken(""))
    (fakeDb, monitoring)
  }

  private lazy val t1 = TrackStock(Some(1), 1, "figi1", 1, stopLoss = 10, takeProfit = 20)
  private lazy val t2 = TrackStock(Some(2), 2, "figi2", 1, stopLoss = 80, takeProfit = 100)
  private lazy val t3 = TrackStock(Some(3), 1, "figi2", 1, stopLoss = 70, takeProfit = 81)
  private lazy val t4 = TrackStock(Some(4), 3, "figi3", 1, stopLoss = 10, takeProfit = 13)
  private lazy val t0 = TrackStock(None, 4, "figi5", 1, stopLoss = 100, takeProfit = 120)

  "filterStocksForSale" should "filter problem prices exclude figi without price" in {
    StocksMonitoringImpl.filterStocksForSale(
      Seq(t1, t2, t3, t4),
      Map("figi1" -> 15, "figi2" -> 80)
    ) shouldEqual Seq(t2)
  }

  "convertToNotifications" should "skip stocks without id" in {
    StocksMonitoringImpl.convertToNotifications(
      Seq(t1, t0)
    ) shouldEqual Seq(Notification(None, 1, 1))
  }

  "markSoldStocksUntrackedInDb" should "mark only correctly responses with id" in {
    val (fakeDb, monitoring) = suite
    val response = generateResponse(PlacedMarketOrder("", "", "", None, None, 1, 1))

    fakeDb.stocksTable.addAll(Seq(1L -> t1, 2L -> t2))
    monitoring.markSoldStocksUntrackedInDb(
      Seq(
        Left(OpenApiResponseError("")),
        Right(None -> response),
        Right(Some(1L) -> response),
      )
    ).isRight.runSyncUnsafe() shouldEqual true
    fakeDb.stocksTable.readOnlySnapshot().toSeq shouldEqual
      Seq(1L -> t1.copy(active = false), 2L -> t2)
  }

  "saleStocks" should "mark Stocks untracked in db" in {
    val (fakeDb, monitoring) = suite

    fakeDb.stocksTable.addAll(Seq(1L -> t1, 2L -> t2, 3L -> t3))
    monitoring.saleStocks(Seq(t2, t1)).isRight.runSyncUnsafe() shouldEqual true
    fakeDb.stocksTable.readOnlySnapshot().toSeq shouldEqual Seq(
      1L -> t1.copy(active = false),
      2L -> t2.copy(active = false),
      3L -> t3,
    )
  }

  "getStockPrices" should "return map of prices" in {
    val (_, monitoring) = suite

    monitoring.getStockPrices(Seq("figi1", "figi2"))
      .runSyncUnsafe() shouldEqual Map("figi1" -> 50, "figi2" -> 50)
  }
}
