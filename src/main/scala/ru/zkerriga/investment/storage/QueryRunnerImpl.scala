package ru.zkerriga.investment.storage

import cats.data.EitherT
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.Backend
import monix.eval.Task
import scala.concurrent.Future

import ru.zkerriga.investment.exceptions.DatabaseError


class QueryRunnerImpl(db: Backend#Database) extends QueryRunner[Task] {

  private def converter[A](future: Future[A]): Task[A] =
    Task.deferFutureAction { implicit scheduler => future }

  def run[A, E <: Effect](dio: DIO[A, E]): EitherT[Task, DatabaseError, A] =
    EitherT(
      converter(db.run(dio))
        .redeem(_ => Left(DatabaseError()), Right.apply)
    )

}