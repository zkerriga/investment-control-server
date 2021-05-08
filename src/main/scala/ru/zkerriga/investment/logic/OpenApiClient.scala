package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._


/**
 * Required functions from Tinkoff OpenAPI
 * https://tinkoffcreditsystems.github.io/invest-openapi/swagger-ui
 */
trait OpenApiClient {
  def `/sandbox/register`(token: TinkoffToken): Task[TinkoffResponse[Empty]]

  def `/market/stocks`(token: TinkoffToken): Task[TinkoffResponse[Stocks]]

  def `/orders/market-order`(token: TinkoffToken, figi: String, marketOrder: MarketOrderRequest): Task[TinkoffResponse[PlacedMarketOrder]]

  def `/market/orderbook`(token: TinkoffToken, figi: String): Task[TinkoffResponse[OrderBook]]
}
