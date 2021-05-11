package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.queries.{NotificationsQueryRepository, TrackStocksQueryRepository}


class ClientDaoImpl(queryRunner: QueryRunner[Task]) extends ClientDao {

  override def registerStock(clientId: Long, order: StockOrder): Task[Long] =
    queryRunner.run(TrackStocksQueryRepository.addTrackStock(clientId, order))

  override def getAllNotificationsAndMarkThemSent(clientId: Long): Task[Seq[(Notification, TrackStock)]] =
    Task.deferAction { implicit scheduler =>
      queryRunner.run(
        for {
          seq <- NotificationsQueryRepository.getAllUnsentNotificationsWithTrackStock(clientId)
          _ <- NotificationsQueryRepository.markNotificationsAsSent(clientId)
        } yield seq
      )
    }

}
