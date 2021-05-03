package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers
import NotificationsQueryRepository._

class NotificationsQueryRepositoryTest extends DatabaseSuite with Matchers {
  test("add new notification should return id") {
    for {
      res <- addNotification(1, "Third hello!")
    } yield assert(res >= 0L)
  }
}
