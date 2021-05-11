package ru.zkerriga.investment.storage

import slick.dbio.Effect
import slick.jdbc.PostgresProfile.Backend
import scala.concurrent.Future


abstract class QueryRunner[F[_]](db: Backend#Database) {
  protected def converter[A]: Future[A] => F[A]

  def run[A, E <: Effect](dio: DIO[A, E]): F[A] =
    converter(db.run(dio))
}
