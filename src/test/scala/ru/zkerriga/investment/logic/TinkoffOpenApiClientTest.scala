package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import monix.execution.Scheduler
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite

import ru.zkerriga.investment.entities.openapi.Stock
import ru.zkerriga.investment.entities.{StockOrder, TinkoffToken}


trait TinkoffOpenApiClientTest extends AsyncFunSuite with BeforeAndAfterAll {

  implicit def as: ActorSystem
  implicit def s: Scheduler

  def api: OpenApiClient
  def validToken: TinkoffToken

  test("Unmarshal should works") {
    (for {
      _ <- api.`/sandbox/register`(validToken)
      stocks <- api.`/market/stocks`(validToken)
      figi = stocks.payload.instruments.collectFirst{ case Stock(figi, _, _, _, _, _, "USD", _) => figi }
      buyRes <- api.`/orders/market-order`(validToken, StockOrder(figi.getOrElse(""), 1, 10.0, 100.0))
    } yield {
      assert(stocks.payload.total > 0)
      assert(figi.nonEmpty)
      assert(buyRes.payload.status === "Fill")
    }).runToFuture
  }

}
