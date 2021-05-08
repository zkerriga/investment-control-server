package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.storage.entities.{Client, Notification, TrackStock}


trait ClientsDao {
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
  def updateClientToken(clientId: Long, token: String): Task[Unit]

  /**
   * Adds information about the purchased asset to the database for further tracking
   * @return the record id
   */
  def registerStock(clientId: Long, order: StockOrder): Task[Long]

  /**
   * Retrieves a list of notifications related to stock-orders that have been sold
   * from the database. All notifications are placed sent.
   */
  def getAllNotificationsAndMarkThemSent(clientId: Long): Task[Seq[(Notification, TrackStock)]]
}
