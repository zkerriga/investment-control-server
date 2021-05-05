package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}
import NotificationsQueryRepository._


class NotificationsQueryRepositoryTest extends DatabaseSuite with Matchers {
  test("add new notification should return id") {
    for {
      res <- addNotifications(Seq(Notification(clientId = 1, trackStockId = 1)))
    } yield assert(res.getOrElse(0) > 0)
  }

  test("get notifications with trackStocks") {
    for {
      empty <- getAllUnsentNotificationsWithTrackStock(2)
      notifiesWithTrackStocks <- getAllUnsentNotificationsWithTrackStock(1)
    } yield {
      assert(empty.isEmpty)
      notifiesWithTrackStocks shouldBe Seq(
        sampleNotifications.head -> sampleTrackStocks.head,
        sampleNotifications(1) -> sampleTrackStocks(1)
      )
    }
  }

  test("mark as sent") {
    for {
      notifies2 <- getAllUnsentNotificationsWithTrackStock(1)
      _ <- markNotificationsAsSent(1)
      empty <- getAllUnsentNotificationsWithTrackStock(1)
    } yield {
      assert(notifies2.size === 2)
      assert(empty.isEmpty)
    }
  }
}
