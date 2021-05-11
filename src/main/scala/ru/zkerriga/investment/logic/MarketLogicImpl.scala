package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.{NotEnoughBalance, PageNotFound}
import ru.zkerriga.investment.entities.{StockOrder, VerifiedClient}
import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, PlacedMarketOrder, Stocks}
import ru.zkerriga.investment.storage.ClientDao


class MarketLogicImpl(dao: ClientDao, openApiClient: OpenApiClient) extends MarketLogic {

  override def getStocks(client: VerifiedClient, page: Int, onPage: Int): Task[Stocks] =
    openApiClient.`/market/stocks`(client.token) flatMap { response =>
      val resultStocks = response.payload.instruments
        .slice(onPage * (page - 1), onPage * page)

      if (resultStocks.isEmpty)
        Task.raiseError(PageNotFound())
      else
        Task.now(response.payload.copy(
          total = resultStocks.size,
          instruments = resultStocks
        ))
    }

  override def buyStocks(client: VerifiedClient, order: StockOrder): Task[PlacedMarketOrder] =
    openApiClient.`/orders/market-order`(client.token, order.figi, MarketOrderRequest(order.lots, "Buy"))
      .map(response => response.payload)
      .onErrorFallbackTo(Task.raiseError(NotEnoughBalance())) <*
        dao.registerStock(client.id, order)

}
