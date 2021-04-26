package com.zkerriga.server
package database

import monix.eval.Task
import slick.lifted.TableQuery
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

object ServerDatabase {
  private val clients = TableQuery[ClientsTable]
  private val db = Database.forConfig("h2mem1")

  private val initSchema =
    (clients.schema).create /* todo: use flyway to create db */

  private val setupFuture: Future[Unit] = db.run(initSchema)

  private def run[A, E <: Effect](dio: DIO[A, E])(implicit ec: ExecutionContext): Future[A] =
    setupFuture.flatMap{ _ => db.run(dio) }

  def close(): Future[Unit] = Future.successful(db.close())

  def registerClient(login: String, passwordHash: String)(implicit ec: ExecutionContext): Future[Int] =
    run(ClientsQueryRepository.addClient(login, passwordHash))

}