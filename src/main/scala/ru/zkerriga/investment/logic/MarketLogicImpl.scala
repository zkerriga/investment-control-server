package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.{DatabaseError, NotEnoughBalance, OpenApiResponseError, PageNotFound}
import ru.zkerriga.investment.entities.{StockOrder, VerifiedClient}
import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, PlacedMarketOrder, Stocks, TinkoffResponse}
import ru.zkerriga.investment.storage.ClientDao


class MarketLogicImpl(dao: ClientDao, openApiClient: OpenApiClient) extends MarketLogic {

  def sliceStocks(stocks: Stocks, page: Int, onPage: Int): Either[PageNotFound, Stocks] = {
    val sliced = stocks.instruments.slice(onPage * (page - 1), onPage * page)
    Either.cond(sliced.isEmpty, Stocks(sliced.size, sliced), PageNotFound())
  }

  override def getStocks(client: VerifiedClient, page: Int, onPage: Int): EitherT[Task, Either[OpenApiResponseError, PageNotFound], Stocks] =
    openApiClient.`/market/stocks`(client.token)
      .leftMap(Left.apply)
      .flatMap {
        case TinkoffResponse(_, _, stocks) =>
          EitherT.fromEither[Task](sliceStocks(stocks, page, onPage))
            .leftMap(Right.apply)
      }

  def registerStockInDb(id: Long, order: StockOrder): EitherT[Task, DatabaseError, Unit] =
    dao.registerStock(id, order).map(_ => ())

  override def buyStocks(client: VerifiedClient, order: StockOrder): EitherT[Task, Either[DatabaseError, NotEnoughBalance], PlacedMarketOrder] =
    openApiClient.`/orders/market-order`(client.token, order.figi, MarketOrderRequest(order.lots, "Buy"))
      .leftMap(_ => Right(NotEnoughBalance()))
      .flatMap {
        case TinkoffResponse(_, _, response) =>
          registerStockInDb(client.id, order)
            .leftMap(err => Left(err).withRight[NotEnoughBalance])
            .map(_ => response)
      }

}
