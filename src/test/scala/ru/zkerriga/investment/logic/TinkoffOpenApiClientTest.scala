package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import monix.execution.Scheduler
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite

import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, Stock}
import ru.zkerriga.investment.entities.TinkoffToken


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
      buyRes <- api.`/orders/market-order`(validToken, figi.getOrElse(""), MarketOrderRequest(1, "Buy"))
    } yield {
      assert(stocks.payload.total > 0)
      assert(figi.nonEmpty)
      assert(buyRes.payload.status === "Fill")
    }).runToFuture
  }

}
