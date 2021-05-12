package ru.zkerriga.investment.storage

import cats.data.EitherT
import slick.dbio.Effect

import ru.zkerriga.investment.exceptions.DatabaseError


trait QueryRunner[F[_]] {
  def run[A, E <: Effect](dio: DIO[A, E]): EitherT[F, DatabaseError, A]
}
