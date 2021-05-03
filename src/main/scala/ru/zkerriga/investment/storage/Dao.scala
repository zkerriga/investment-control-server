package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.storage.entities.Client


trait Dao {
  /**
   * Close the connection to the database
   */
  def close(): Task[Unit]

  /**
   * Adding a client to the Clients table
   * @param username a client's username
   * @param hash a client's hash of password
   * @return new id of client
   */
  def registerClient(username: String, hash: String): Task[Long]

  /**
   * Search for a client by username
   * @return the client, if it is found
   */
  def findClientByUsername(username: String): Task[Option[Client]]

  /**
   * Updates the token of an existing client
   * @param clientId id of an existing(!) client from the database
   */
  def updateClientToken(clientId: Long, token: String): Task[Int]
}
