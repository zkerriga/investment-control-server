package ru.zkerriga.investment

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._
import ru.zkerriga.investment.exceptions.OpenApiResponseError
import ru.zkerriga.investment.logic.OpenApiClient


object TestOpenApiClient extends OpenApiClient {

  val stocks22: Stocks = Stocks(
    22,
    Seq.fill(22)(Stock("A1B2", "", "", None, lot = 10, None, currency = "USD", name = "A Inc."))
  )

  override def `/sandbox/register`(token: TinkoffToken): EitherT[Task, OpenApiResponseError, TinkoffResponse[Empty]] =
    EitherT.rightT(TinkoffResponse[Empty]("", "Ok", Empty()))

  override def `/market/stocks`(token: TinkoffToken): EitherT[Task, OpenApiResponseError, TinkoffResponse[Stocks]] =
    EitherT.rightT(TinkoffResponse[Stocks]("", "Ok", stocks22))

  override def `/orders/market-order`(token: TinkoffToken, figi: String, marketOrder: MarketOrderRequest): EitherT[Task, OpenApiResponseError, TinkoffResponse[PlacedMarketOrder]] =
    EitherT.rightT(TinkoffResponse[PlacedMarketOrder](
      "", "Ok",
      PlacedMarketOrder(
        orderId = "1",
        operation = marketOrder.operation,
        status = "Fill",
        rejectReason = None,
        message = None,
        requestedLots = marketOrder.lots,
        executedLots = marketOrder.lots
      )
    ))

  override def `/market/orderbook`(token: TinkoffToken, figi: String): EitherT[Task, OpenApiResponseError, TinkoffResponse[OrderBook]] =
    EitherT.rightT(TinkoffResponse[OrderBook]("", "Ok",
      OrderBook(figi = figi, depth = 20, bids = Seq.empty, asks = Seq.empty, lastPrice = Some(50.0))
    ))
}
