package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.{NotificationMessage, Notifications, StockOrder, VerifiedClient}
import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import ru.zkerriga.investment.storage.ClientDao


class NotificationLogicImpl(dao: ClientDao) extends NotificationLogic {

  override def getAllNotifications(client: VerifiedClient): EitherT[Task, DatabaseError, Notifications] =
    dao.getAllNotificationsAndMarkThemSent(client.id) map NotificationLogicImpl.createNotifications

}

object NotificationLogicImpl {

  def createNotifications(notificationsInfoWithTrackStocks: Seq[(Notification, TrackStock)]): Notifications =
    Notifications(
      total = notificationsInfoWithTrackStocks.size,
      notifications = notificationsInfoWithTrackStocks map {
        /* todo: add info to notification */
        case (_, trackStock) => NotificationMessage(StockOrder.from(trackStock), "Sold")
      }
    )

}
