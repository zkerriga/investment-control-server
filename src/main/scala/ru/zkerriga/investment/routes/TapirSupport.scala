package ru.zkerriga.investment.routes

import monix.eval.Task
import ru.zkerriga.investment.api.ExceptionResponse

trait TapirSupport {
  def handleErrors[T](task: Task[T]): Task[Either[ExceptionResponse, T]] =
    task.redeem(
      error => Left(ExceptionResponse(error.getMessage)),
      result => Right(result)
    )
}
