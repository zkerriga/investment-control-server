package ru.zkerriga.investment.monitoring

import cats.data.EitherT
import monix.eval.Task
import scala.collection.concurrent.TrieMap

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.MonitoringDao
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class FakeMonitoringDao extends MonitoringDao {

  val notificationsTable: TrieMap[Long, Notification] = new TrieMap[Long, Notification]
  val stocksTable: TrieMap[Long, TrackStock] = new TrieMap[Long, TrackStock]

  override def getAllTrackedStocks: EitherT[Task, DatabaseError, Seq[TrackStock]] =
    EitherT.rightT(stocksTable.readOnlySnapshot().collect {
      case (_, t) if t.active => t
    }.toSeq)

  override def markStocksUntracked(stockIds: Seq[Long]): EitherT[Task, DatabaseError, Unit] =
    EitherT.rightT(for {
      id <- stockIds
      t <- stocksTable.get(id)
    } yield stocksTable.update(id, t.copy(active = false)))

  override def addNotifications(notifications: Seq[Notification]): EitherT[Task, DatabaseError, Unit] =
    EitherT.rightT(notificationsTable.addAll(
      notifications.collect {
        case n @ Notification(Some(id), _, _, _, _) => id -> n
      }
    ))
}
