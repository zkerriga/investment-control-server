package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.storage.entities.Client


trait RegisterLogic {

  /**
   * Register a new client with a unique login.
   *
   * @return the username if the operation was successful,
   *         and a LoginAlreadyExist exception otherwise
   */
  def registerClient(login: Login): Task[String]

  /**
   * Updates the token of an existing token.
   *
   * @param client must exist in the database!
   * @return the username if the operation was successful, and a InvalidToken exception
   *         otherwise
   */
  def updateToken(client: Client, token: TinkoffToken): Task[String]

}
