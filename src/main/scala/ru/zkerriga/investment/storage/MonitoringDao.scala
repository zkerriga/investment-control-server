package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


trait MonitoringDao {
  type FIGI = String

  def getAllTrackedStocks: Task[Map[FIGI, Seq[TrackStock]]]

  def markStocksUntracked(stockIds: Seq[Long]): Task[Unit]

  def addNotifications(notifications: Seq[Notification]): Task[Unit]
}
