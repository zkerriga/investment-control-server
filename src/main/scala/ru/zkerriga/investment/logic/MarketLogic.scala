package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.{StockOrder, VerifiedClient}
import ru.zkerriga.investment.entities.openapi.{PlacedMarketOrder, Stocks}
import ru.zkerriga.investment.exceptions.{DatabaseError, NotEnoughBalance, OpenApiResponseError, PageNotFound}


trait MarketLogic {

   /**
   * @param page   stock page based on the number of shares per page
   * @param onPage number of shares per page
   * @return a list of stocks that can be purchased on the exchange,
   *         and a PageNotFound if there is nothing on the page
   */
  def getStocks(client: VerifiedClient, page: Int, onPage: Int): EitherT[Task, Either[OpenApiResponseError, PageNotFound], Stocks]

  /**
   * Buys stocks using OpenAPI and registers asset tracking in the database
   * @return a response from OpenAPI with information about the request if success,
   *         and a NotEnoughBalance exception otherwise
   */
  def buyStocks(client: VerifiedClient, stockOrder: StockOrder): EitherT[Task, Either[DatabaseError, NotEnoughBalance], PlacedMarketOrder]

}
