package ru.zkerriga.investment.storage

import slick.jdbc.H2Profile.api._
import monix.eval.Task

import ru.zkerriga.investment.storage.entities.Client


object ServerDatabase {
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

  def registerClient(login: String, passwordHash: String): Task[Long] =
    run(ClientsQueryRepository.addClient(login, passwordHash))

  def findClientByLogin(login: String): Task[Option[Client]] =
    run(ClientsQueryRepository.findByLogin(login))

  def updateClientToken(clientId: Long, token: String): Task[Int] =
    run(ClientsQueryRepository.updateToken(clientId, token))
}
