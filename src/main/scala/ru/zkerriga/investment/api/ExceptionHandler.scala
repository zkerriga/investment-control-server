package ru.zkerriga.investment.api

import ru.zkerriga.investment.exceptions.ServerError


trait ExceptionHandler[F[_], M[T[_], _, _]] {
  def handle[A, E <: ServerError](logic: M[F, E, A]): F[Either[ExceptionResponse, A]]

  def recover[A, E <: ServerError](logic: M[F, E, A]): M[F, ExceptionResponse, A]
}
