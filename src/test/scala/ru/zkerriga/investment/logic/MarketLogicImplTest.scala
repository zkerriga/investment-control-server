package ru.zkerriga.investment.logic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.TestOpenApiClient
import ru.zkerriga.investment.entities.{StockOrder, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.entities.openapi.{Stock, Stocks}
import ru.zkerriga.investment.exceptions.PageNotFound
import ru.zkerriga.investment.storage.entities.TrackStock


class MarketLogicImplTest extends AnyFlatSpec with Matchers {

  import monix.execution.Scheduler.Implicits.global

  def suite: (FakeClientDao, MarketLogicImpl) = {
    val fakeDb = new FakeClientDao
    val logic = new MarketLogicImpl(fakeDb, TestOpenApiClient)
    (fakeDb, logic)
  }

  private lazy val s1 = Stock("A1B2", "", "", None, lot = 10, None, "USD", "A Inc.")
  private lazy val client = VerifiedClient(1, "z", TinkoffToken(""))

  behavior of "sliceStocks"

  private lazy val stocks20 = Stocks(20, Seq.fill(20)(s1))

  it should "return stocks" in {
    MarketLogicImpl.sliceStocks(stocks20, 1, 1) shouldEqual
      Right(Stocks(1, Seq(s1)))

    MarketLogicImpl.sliceStocks(stocks20, 1, 100) shouldEqual
      Right(stocks20)

    MarketLogicImpl.sliceStocks(stocks20, 1, 20) shouldEqual
      Right(stocks20)

    MarketLogicImpl.sliceStocks(stocks20, 20, 1) shouldEqual
      Right(Stocks(1, stocks20.instruments.takeRight(1)))
  }
  it should "return PageNotFound" in {
    lazy val pageNotFound = PageNotFound()

    MarketLogicImpl.sliceStocks(stocks20, 2, 20) shouldEqual
      Left(pageNotFound)

    MarketLogicImpl.sliceStocks(stocks20, 3, 10) shouldEqual
      Left(pageNotFound)
  }

  "getStocks" should "get only 10 stocks" in {
    val (_, market) = suite

    market.getStocks(client, 1, 10).value.runSyncUnsafe() shouldEqual
      Right(TestOpenApiClient.stocks22.copy(10, TestOpenApiClient.stocks22.instruments.take(10)))
  }

  behavior of "buyStocks"

  it should "register stocks in db" in {
    val (fakeDb, market) = suite

    val order1 = StockOrder("figi", lots = 1, 10.2, 20.1)
    market.buyStocks(client, order1)
      .map(_.executedLots).value.runSyncUnsafe() shouldEqual
        Right(1)

    fakeDb.stocksTable.readOnlySnapshot
      .toSeq shouldEqual Seq(0 -> TrackStock(Some(0), client.id, "figi", 1, 10.2, 20.1))

    market.buyStocks(client, StockOrder("figi2", lots = 100, 10.2, 20.1))
      .map(_.executedLots).value.runSyncUnsafe() shouldEqual
        Right(100)

    fakeDb.stocksTable.readOnlySnapshot
      .toSeq shouldEqual
      Seq(
        0 -> TrackStock(Some(0), client.id, "figi", 1, 10.2, 20.1),
        1 -> TrackStock(Some(1), client.id, "figi2", 100, 10.2, 20.1),
      )

  }
}
