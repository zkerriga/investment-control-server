package ru.zkerriga.investment.api

import monix.eval.Task
import ru.zkerriga.investment.entities.Login


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
}
