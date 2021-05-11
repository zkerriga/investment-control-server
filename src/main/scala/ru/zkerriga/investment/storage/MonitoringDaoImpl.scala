package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.queries.{NotificationsQueryRepository, TrackStocksQueryRepository}


class MonitoringDaoImpl(queryRunner: QueryRunner[Task]) extends MonitoringDao {

  override def getAllTrackedStocks: Task[Seq[TrackStock]] =
    queryRunner.run(TrackStocksQueryRepository.getAllTrackedStocks)

  override def markStocksUntracked(stockIds: Seq[Long]): Task[Unit] =
    queryRunner.run(
      DIO.sequence(stockIds map (id => TrackStocksQueryRepository.markStockUntracked(id)))
    ) flatMap (_ => Task.unit)

  override def addNotifications(notifications: Seq[Notification]): Task[Unit] =
    queryRunner.run(
      NotificationsQueryRepository.addNotifications(notifications)
    ) flatMap (_ => Task.unit)

}
