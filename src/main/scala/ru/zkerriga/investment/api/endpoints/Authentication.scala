package ru.zkerriga.investment.api.endpoints

import cats.data.EitherT
import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.model.UsernamePassword
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.entities.VerifiedClient
import ru.zkerriga.investment.logic.VerifyLogic


trait Authentication {

  type AuthFunction = UsernamePassword => Future[Either[ExceptionResponse, VerifiedClient]]

  def authorizeF(verifyLogic: VerifyLogic, eh: ExceptionHandler[Task, EitherT])
                (implicit s: Scheduler): AuthFunction =
    credentials =>
        (for {
          client    <- eh.recover(verifyLogic.verifyCredentials(credentials))
          verified  <- eh.recover(verifyLogic.verifyToken(client))
        } yield verified).value.runToFuture

}
