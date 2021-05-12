package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.{InternalError, NotEnoughBalance, PageNotFound}
import ru.zkerriga.investment.entities.{StockOrder, VerifiedClient}
import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, PlacedMarketOrder, Stocks, TinkoffResponse}
import ru.zkerriga.investment.storage.ClientDao


class MarketLogicImpl(dao: ClientDao, openApiClient: OpenApiClient) extends MarketLogic {

  override def getStocks(client: VerifiedClient, page: Int, onPage: Int): Task[Stocks] =
    (openApiClient.`/market/stocks`(client.token) flatMapF {
      case TinkoffResponse(_, _, stocks) =>
        val result = stocks.instruments.slice(onPage * (page - 1), onPage * page)

        if (result.isEmpty)
          Task.raiseError(PageNotFound())
        else
          Task.now(Right(Stocks(result.size, result)))
    }).valueOrF(_ => Task.raiseError(InternalError("getStocks")))

  override def buyStocks(client: VerifiedClient, order: StockOrder): Task[PlacedMarketOrder] =
    openApiClient.`/orders/market-order`(client.token, order.figi, MarketOrderRequest(order.lots, "Buy"))
      .map(_.payload)
      .valueOrF(_ => Task.raiseError[PlacedMarketOrder](NotEnoughBalance())) <*
        dao.registerStock(client.id, order)

}
