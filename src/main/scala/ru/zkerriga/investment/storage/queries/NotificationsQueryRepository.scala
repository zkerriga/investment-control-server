package ru.zkerriga.investment.storage.queries

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import ru.zkerriga.investment.storage.DIO
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.tables.NotificationsTable


object NotificationsQueryRepository {
  val AllNotifications = TableQuery[NotificationsTable]

  def addNotifications(notifications: Seq[Notification]): DIO[Option[Int], Effect.Write] =
    AllNotifications ++= notifications

  private def findAllUnsent(clientId: Long): Query[NotificationsTable, Notification, Seq] =
    AllNotifications
      .filter(n => n.clientId === clientId && !n.sent)

  def getAllUnsentNotificationsWithTrackStock(clientId: Long): DIO[Seq[(Notification, TrackStock)], Effect.Read] =
    findAllUnsent(clientId)
      .join(TrackStocksQueryRepository.AllTrackStocks)
      .on(_.trackStockId === _.id)
      .result

  def markNotificationsAsSent(clientId: Long): DIO[Int, Effect.Write] =
    findAllUnsent(clientId)
      .map(_.sent)
      .update(true)
}
