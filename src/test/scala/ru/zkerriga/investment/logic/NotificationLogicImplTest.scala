package ru.zkerriga.investment.logic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.entities._
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class NotificationLogicImplTest extends AnyFlatSpec with Matchers {

  import monix.execution.Scheduler.Implicits.global

  private lazy val client = VerifiedClient(1, "z", TinkoffToken(""))

  def suite: (FakeClientDao, NotificationLogicImpl) = {
    val fakeDb = new FakeClientDao
    val logic = new NotificationLogicImpl(fakeDb)
    (fakeDb, logic)
  }

  behavior of "getAllNotifications"

  it should "get empty notifications" in {
    val (_, logic) = suite

    logic.getAllNotifications(client).value.runSyncUnsafe() shouldEqual
      Right(Notifications(0, Seq.empty))
  }

  it should "get notifications" in {
    val (fakeDb, logic) = suite

    val t1 = TrackStock(Some(1), client.id, "figi1", lots = 1, stopLoss = 10.1, takeProfit = 20.2)
    val t2 = TrackStock(Some(2), client.id, "figi2", lots = 2, stopLoss = 20.1, takeProfit = 40.0)

    fakeDb.stocksTable.addAll(
      Seq(
        1L -> t1,
        2L -> t2,
      )
    )
    fakeDb.notificationsTable.addAll(
      Seq(
        1L -> Notification(Some(1), client.id, 1),
        2L -> Notification(Some(2), client.id, 2, sent = true)
      )
    )
    logic.getAllNotifications(client).value.runSyncUnsafe() shouldEqual
      Right(Notifications(
        1, Seq(NotificationMessage(StockOrder.from(t1), "Sold"))
      ))
  }

}
