package ru.zkerriga.investment.api

import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import ru.zkerriga.investment.exceptions.{ServerError, ServerInternalError, ServiceError}


class ExceptionHandlerEitherTImpl extends ExceptionHandler[Task, EitherT] with LazyLogging {

  private lazy val internalErrorResponse = ExceptionResponse("Internal error")

  private def recoverF[E <: ServerError]: E => ExceptionResponse = {
    case internalError: ServerInternalError =>
      logger.info(internalError.getMessage)
      internalErrorResponse
    case logicError: ServiceError => ExceptionResponse(logicError.getMessage)
    case criticalError =>
      logger.info(s"Unknown critical error: ${criticalError.getMessage}")
      internalErrorResponse
  }

  override def handle[A, E <: ServerError](logic: EitherT[Task, E, A]): Task[Either[ExceptionResponse, A]] =
    recover(logic).value

  override def recover[A, E <: ServerError](logic: EitherT[Task, E, A]): EitherT[Task, ExceptionResponse, A] =
    logic.leftMap(recoverF)
}
