package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.queries.{NotificationsQueryRepository, TrackStocksQueryRepository}


class ClientDaoImpl(queryRunner: QueryRunner[Task]) extends ClientDao {

  override def registerStock(clientId: Long, order: StockOrder): EitherT[Task, DatabaseError, Long] =
    queryRunner.run(TrackStocksQueryRepository.addTrackStock(clientId, order))

  override def getAllNotificationsAndMarkThemSent(clientId: Long): EitherT[Task, DatabaseError, Seq[(Notification, TrackStock)]] =
    EitherT(Task.deferAction { implicit scheduler =>
      queryRunner.run(
        for {
          seq <- NotificationsQueryRepository.getAllUnsentNotificationsWithTrackStock(clientId)
          _ <- NotificationsQueryRepository.markNotificationsAsSent(clientId)
        } yield seq
      ).value
    })

}
