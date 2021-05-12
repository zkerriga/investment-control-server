package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.Client


trait LoginDao {
  /**
   * Adding a client to the Clients table
   * @param username a client's username
   * @param hash a client's hash of password
   * @return new id of client
   */
  def registerClient(username: String, hash: String): EitherT[Task, DatabaseError, Long]

  /**
   * Search for a client by username
   * @return the client, if it is found
   */
  def findClientByUsername(username: String): EitherT[Task, DatabaseError, Option[Client]]

  /**
   * Updates the token of an existing client
   * @param clientId id of an existing(!) client from the database
   */
  def updateClientToken(clientId: Long, token: String): EitherT[Task, DatabaseError, Unit]
}
