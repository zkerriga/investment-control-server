package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.model.UsernamePassword
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.entities.VerifiedClient
import ru.zkerriga.investment.logic.VerifyLogic


trait Authentication {
  type AuthFunction = UsernamePassword => Future[Either[ExceptionResponse, VerifiedClient]]

  def authorizeF(verifyLogic: VerifyLogic, exceptionHandler: ExceptionHandler[Task])
                (implicit s: Scheduler): AuthFunction =
    credentials =>
      exceptionHandler.handle(
        verifyLogic.verifyCredentials(credentials) flatMap { client =>
          verifyLogic.verifyToken(client)
        }
      ).runToFuture
}
