package ru.zkerriga.investment.api

import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.openapi.Stock
import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.storage.Client


/**
 * Called in Routes,
 * it is used to transfer authority to specific parts of the server.
 */
trait ServiceApi {
  /**
   * Register a new client with a unique login.
   * @return a login if the operation was successful,
   *         and a LoginAlreadyExist exception otherwise
   */
  def registerClient(login: Login): Task[String]

  /**
   * Verifies the client's credentials.
   * @return Information about the client from the database if successful,
   *         and a IncorrectCredentials exception otherwise
   */
  def verifyCredentials(credentials: UsernamePassword): Task[Client]

  /**
   * Checks the client token.
   * @return the client if the token exists, and a TokenDoesNotExist exception otherwise
   */
  def verifyToken(client: Client): Task[VerifiedClient]

  /**
   * Updates the token of an existing token.
   * @param client must exist in the database!
   * @return a login if the operation was successful, and a InvalidToken exception
   *         otherwise
   */
  def updateToken(client: Client, token: TinkoffToken): Task[String]
}
