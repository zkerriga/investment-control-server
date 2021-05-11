package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.entities.{NotificationMessage, Notifications, StockOrder, VerifiedClient}
import ru.zkerriga.investment.storage.ClientDao


class NotificationLogicImpl(dao: ClientDao) extends NotificationLogic {

  override def getAllNotifications(client: VerifiedClient): Task[Notifications] =
    dao.getAllNotificationsAndMarkThemSent(client.id) map { seq =>
      Notifications(
        total = seq.size,
        notifications = seq map {
          /* todo: add info to notification */
          case (_, trackStock) => NotificationMessage(StockOrder.from(trackStock), "Sold")
        }
      )
    }

}
