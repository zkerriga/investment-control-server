package ru.zkerriga.investment.api

import ru.zkerriga.investment.exceptions.ServerError


trait ExceptionHandler[F[_], M[T[_], _, _]] {
  def handle[A, E <: ServerError](logic: M[F, E, A]): F[Either[ExceptionResponse, A]]
  def handleEither[A, E1 <: ServerError, E2 <: ServerError](logic: M[F, Either[E1, E2], A]): F[Either[ExceptionResponse, A]]

  def recover[A, E <: ServerError](logic: M[F, E, A]): M[F, ExceptionResponse, A]
  def recoverEither[A, E1 <: ServerError, E2 <: ServerError](logic: M[F, Either[E1, E2], A]): M[F, ExceptionResponse, A]
}
