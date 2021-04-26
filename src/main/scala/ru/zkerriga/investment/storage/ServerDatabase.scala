package ru.zkerriga.investment.storage

import slick.lifted.TableQuery
import slick.jdbc.H2Profile.api._

import monix.eval.Task

object ServerDatabase {
  private val clients = TableQuery[ClientsTable]
  private val db = Database.forConfig("h2mem1")

  private val initSchema =
    (clients.schema).create /* todo: use flyway to create db */

  private val setupFuture: Task[Unit] = Task.fromFuture(db.run(initSchema))

  private def run[A, E <: Effect](dio: DIO[A, E]): Task[A] =
    setupFuture.flatMap{ _ => Task.fromFuture(db.run(dio)) }

  def close(): Task[Unit] = Task(db.close())

  def registerClient(login: String, passwordHash: String): Task[Int] =
    run(ClientsQueryRepository.addClient(login, passwordHash))

}