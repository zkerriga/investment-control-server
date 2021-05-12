package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.OpenApiResponseError
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._


/**
 * Required functions from Tinkoff OpenAPI
 * https://tinkoffcreditsystems.github.io/invest-openapi/swagger-ui
 */
trait OpenApiClient {
  def `/sandbox/register`(token: TinkoffToken): EitherT[Task, OpenApiResponseError, TinkoffResponse[Empty]]

  def `/market/stocks`(token: TinkoffToken): EitherT[Task, OpenApiResponseError, TinkoffResponse[Stocks]]

  def `/orders/market-order`(token: TinkoffToken, figi: String, marketOrder: MarketOrderRequest): EitherT[Task, OpenApiResponseError, TinkoffResponse[PlacedMarketOrder]]

  def `/market/orderbook`(token: TinkoffToken, figi: String): EitherT[Task, OpenApiResponseError, TinkoffResponse[OrderBook]]
}
