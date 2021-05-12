package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.queries.{NotificationsQueryRepository, TrackStocksQueryRepository}


class MonitoringDaoImpl(queryRunner: QueryRunner[Task]) extends MonitoringDao {

  override def getAllTrackedStocks: EitherT[Task, DatabaseError, Seq[TrackStock]] =
    queryRunner.run(TrackStocksQueryRepository.getAllTrackedStocks)

  override def markStocksUntracked(stockIds: Seq[Long]): EitherT[Task, DatabaseError, Unit] =
    queryRunner.run(
      DIO.sequence(stockIds map (id => TrackStocksQueryRepository.markStockUntracked(id)))
    ) map (_ => ())

  override def addNotifications(notifications: Seq[Notification]): EitherT[Task, DatabaseError, Unit] =
    queryRunner.run(
      NotificationsQueryRepository.addNotifications(notifications)
    ) map (_ => ())

}
