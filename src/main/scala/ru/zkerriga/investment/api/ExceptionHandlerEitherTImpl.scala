package ru.zkerriga.investment.api

import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import ru.zkerriga.investment.exceptions.{ServerError, ServerInternalError, ServiceError}


class ExceptionHandlerEitherTImpl extends ExceptionHandler[Task, EitherT] with LazyLogging {

  private lazy val internalErrorResponse = ExceptionResponse("Internal error")

  def recoverF: ServerError => ExceptionResponse = {
    case internalError: ServerInternalError =>
      logger.info(internalError.getMessage)
      internalErrorResponse
    case logicError: ServiceError => ExceptionResponse(logicError.getMessage)
    case criticalError =>
      logger.info(s"Unknown critical error: ${criticalError.getMessage}")
      internalErrorResponse
  }

  def recoverEF: Either[ServerError, ServerError] => ExceptionResponse =
    _.fold(recoverF, recoverF)


  override def handle[A, E <: ServerError](logic: EitherT[Task, E, A]): Task[Either[ExceptionResponse, A]] =
    recover(logic).value

  override def handleEither[A, E1 <: ServerError, E2 <: ServerError](logic: EitherT[Task, Either[E1, E2], A]): Task[Either[ExceptionResponse, A]] =
    recoverEither(logic).value

  override def recover[A, E <: ServerError](logic: EitherT[Task, E, A]): EitherT[Task, ExceptionResponse, A] =
    logic.leftMap(recoverF)

  override def recoverEither[A, E1 <: ServerError, E2 <: ServerError](logic: EitherT[Task, Either[E1, E2], A]): EitherT[Task, ExceptionResponse, A] =
    logic.leftMap(recoverEF)

}
