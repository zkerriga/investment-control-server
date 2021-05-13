package ru.zkerriga.investment.logic

import cats.implicits._
import cats.data.EitherT
import monix.eval.Task
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.ClientDao
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class FakeClientDao extends ClientDao {

  val notificationsTable: TrieMap[Long, Notification] = new TrieMap[Long, Notification]
  private val stocksId = new AtomicInteger(0)
  val stocksTable: TrieMap[Long, TrackStock] = new TrieMap[Long, TrackStock]

  override def registerStock(clientId: Long, o: StockOrder): EitherT[Task, DatabaseError, Long] =
    EitherT.rightT({
      val id = stocksId.getAndIncrement
      stocksTable.addOne(
        (id, TrackStock(Some(id), clientId, o.figi, o.lots, o.stopLoss, o.takeProfit))
      )
      clientId
    })

  override def getAllNotificationsAndMarkThemSent(clientId: Long): EitherT[Task, DatabaseError, Seq[(Notification, TrackStock)]] =
    EitherT.rightT({
      val stocks = stocksTable.readOnlySnapshot
      val result = notificationsTable.readOnlySnapshot.collect {
        case (id, notification) if id === clientId && !notification.sent =>
          (notification, stocks.get(notification.trackStockId))
      }.collect {
        case (notification, Some(trackStock)) => (notification, trackStock)
      }.toSeq
      result.foreach {
        case (n @ Notification(Some(id), _, _, _, _), _) => notificationsTable.update(id, n.copy(sent = true))
      }
      result
    })
}
