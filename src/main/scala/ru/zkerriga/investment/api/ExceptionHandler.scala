package ru.zkerriga.investment.api

import monix.eval.Task


trait ExceptionHandler[F[_]] {
  def handle[A](task: F[A]): F[Either[ExceptionResponse, A]]
}

object ExceptionHandlerForTask {
  def apply(): ExceptionHandler[Task] = new ExceptionHandler[Task] {
    override def handle[A](task: Task[A]): Task[Either[ExceptionResponse, A]] =
      task.redeem(
        error => Left(ExceptionResponse(error.getMessage)),
        result => Right(result)
      )
  }
}
