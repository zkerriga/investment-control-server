package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


trait MonitoringDao {
  type FIGI = String

  def getAllTrackedStocks: Task[Map[FIGI, TrackStock]]

  def markStocksUntracked(stocks: Seq[TrackStock]): Task[Unit]

  def addNotifications(notifications: Seq[Notification]): Task[Unit]
}
