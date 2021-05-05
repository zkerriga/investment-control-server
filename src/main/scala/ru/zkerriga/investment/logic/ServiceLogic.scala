package ru.zkerriga.investment.logic

import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.openapi.{PlacedMarketOrder, Stocks}
import ru.zkerriga.investment.entities.{Login, Notifications, StockOrder, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.storage.entities.Client


/**
 * Called in Routes,
 * it is used to transfer authority to specific parts of the server.
 */
trait ServiceLogic {
  /**
   * Register a new client with a unique login.
   *
   * @return the username if the operation was successful,
   *         and a LoginAlreadyExist exception otherwise
   */
  def registerClient(login: Login): Task[String]

  /**
   * Verifies the client's credentials.
   *
   * @return Information about the client from the database if successful,
   *         and a IncorrectCredentials exception otherwise
   */
  def verifyCredentials(credentials: UsernamePassword): Task[Client]

  /**
   * Checks the client token.
   *
   * @return the client if the token exists, and a TokenDoesNotExist exception otherwise
   */
  def verifyToken(client: Client): Task[VerifiedClient]

  /**
   * Updates the token of an existing token.
   *
   * @param client must exist in the database!
   * @return the username if the operation was successful, and a InvalidToken exception
   *         otherwise
   */
  def updateToken(client: Client, token: TinkoffToken): Task[String]

  /**
   * @param page   stock page based on the number of shares per page
   * @param onPage number of shares per page
   * @return a list of stocks that can be purchased on the exchange,
   *         and a PageNotFound if there is nothing on the page
   */
  def getStocks(client: VerifiedClient, page: Int, onPage: Int): Task[Stocks]

  /**
   * Buys stocks using OpenAPI and registers asset tracking in the database
   * @return a response from OpenAPI with information about the request if success,
   *         and a NotEnoughBalance exception otherwise
   */
  def buyStocks(client: VerifiedClient, stockOrder: StockOrder): Task[PlacedMarketOrder]

  /**
   * Accesses the database and generates a list of notifications for the client
   * @return all notifications for the client
   */
  def getAllNotifications(client: VerifiedClient): Task[Notifications]
}
