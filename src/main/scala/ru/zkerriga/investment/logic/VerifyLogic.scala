package ru.zkerriga.investment.logic

import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.VerifiedClient
import ru.zkerriga.investment.storage.entities.Client


trait VerifyLogic {

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

}
