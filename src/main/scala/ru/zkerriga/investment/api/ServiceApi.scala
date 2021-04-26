package ru.zkerriga.investment.api

import ru.zkerriga.investment.entities.Login

import scala.concurrent.{ExecutionContext, Future}

/**
 * Called in Routes,
 * it is used to transfer authority to specific parts of the server.
 */
trait ServiceApi {
  /**
   * Register a new client with a unique login.
   * @return a positive number if the operation was successful,
   *         and a LoginAlreadyExist exception otherwise
   */
  def registerClient(login: Login)(implicit ec: ExecutionContext): Future[Int]
}
