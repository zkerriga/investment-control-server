package ru.zkerriga.investment.storage

import slick.dbio.Effect
import slick.jdbc.PostgresProfile.Backend
import monix.eval.Task
import scala.concurrent.Future


class QueryRunnerImpl(db: Backend#Database) extends QueryRunner[Task] {

  private def converter[A]: Future[A] => Task[A] =
    future => Task.deferFutureAction { implicit scheduler => future }

  def run[A, E <: Effect](dio: DIO[A, E]): Task[A] = converter(db.run(dio))

}