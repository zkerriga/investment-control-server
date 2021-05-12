package ru.zkerriga.investment.storage

import slick.dbio.Effect


trait QueryRunner[F[_]] {
  def run[A, E <: Effect](dio: DIO[A, E]): F[A]
}
