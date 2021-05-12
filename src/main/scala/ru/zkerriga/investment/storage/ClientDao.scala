package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


trait ClientDao {
  /**
   * Adds information about the purchased asset to the database for further tracking
   * @return the record id
   */
  def registerStock(clientId: Long, order: StockOrder): EitherT[Task, DatabaseError, Long]

  /**
   * Retrieves a list of notifications related to stock-orders that have been sold
   * from the database. All notifications are placed sent.
   */
  def getAllNotificationsAndMarkThemSent(clientId: Long): EitherT[Task, DatabaseError, Seq[(Notification, TrackStock)]]
}
