package ru.zkerriga.investment.storage

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import ru.zkerriga.investment.storage.entities.Notification
import ru.zkerriga.investment.storage.tables.NotificationsTable


private[storage] object NotificationsQueryRepository {
  val AllNotifications = TableQuery[NotificationsTable]

  def addNotifications(notifications: Seq[Notification]): DIO[Option[Int], Effect.Write] =
    AllNotifications ++= notifications
}
