package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


trait MonitoringDao {
  type FIGI = String

  def getAllTrackedStocks: EitherT[Task, DatabaseError, Seq[TrackStock]]

  def markStocksUntracked(stockIds: Seq[Long]): EitherT[Task, DatabaseError, Unit]

  def addNotifications(notifications: Seq[Notification]): EitherT[Task, DatabaseError, Unit]
}
