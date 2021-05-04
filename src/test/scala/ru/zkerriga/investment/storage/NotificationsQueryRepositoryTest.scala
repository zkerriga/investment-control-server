package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.storage.entities.Notification
import NotificationsQueryRepository._

class NotificationsQueryRepositoryTest extends DatabaseSuite with Matchers {
  test("add new notification should return id") {
    for {
      res <- addNotifications(Seq(Notification(clientId = 1, trackStockId = 1)))
    } yield assert(res.getOrElse(0) > 0)
  }
}
