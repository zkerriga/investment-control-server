package ru.zkerriga.investment.storage

import slick.jdbc.H2Profile.api._
import monix.eval.Task

import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.storage.entities.{Client, Notification, TrackStock}


object ServerDatabase extends ClientsDao with MonitoringDao {
  private val clients = ClientsQueryRepository.AllClients
  private val trackStocks = TrackStocksQueryRepository.AllTrackStocks
  private val notifications = NotificationsQueryRepository.AllNotifications

  private val db = Database.forConfig("h2mem1")

  /* todo: use flyway to create db */
  private val initSchema =
    (clients.schema ++ trackStocks.schema ++ notifications.schema).create

  private val setupFuture: Task[Unit] = Task.fromFuture(db.run(initSchema))

  private def run[A, E <: Effect](dio: DIO[A, E]): Task[A] =
    setupFuture.flatMap{ _ => Task.fromFuture(db.run(dio)) }

  def close(): Task[Unit] = Task(db.close())

  def registerClient(username: String, passwordHash: String): Task[Long] =
    run(ClientsQueryRepository.addClient(username, passwordHash))

  def findClientByUsername(username: String): Task[Option[Client]] =
    run(ClientsQueryRepository.findByLogin(username))

  def updateClientToken(clientId: Long, token: String): Task[Unit] =
    run(ClientsQueryRepository.updateToken(clientId, token)) flatMap (_ => Task.unit)

  def registerStock(clientId: Long, order: StockOrder): Task[Long] =
    run(TrackStocksQueryRepository.addTrackStock(clientId, order))

  def getAllNotificationsAndMarkThemSent(clientId: Long): Task[Seq[(Notification, TrackStock)]] =
    Task.deferFutureAction { implicit scheduler =>
      db.run(
        (for {
          seq <- NotificationsQueryRepository.getAllUnsentNotificationsWithTrackStock(clientId)
          _ <- NotificationsQueryRepository.markNotificationsAsSent(clientId)
        } yield seq).transactionally
      )
    }

  def getAllTrackedStocks: Task[Map[ServerDatabase.FIGI, TrackStock]] = ???

  def markStocksUntracked(stocks: Seq[TrackStock]): Task[Unit] = ???

  def addNotifications(notifications: Seq[Notification]): Task[Unit] = ???
}
