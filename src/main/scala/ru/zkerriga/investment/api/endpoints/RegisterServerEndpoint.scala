package ru.zkerriga.investment.api.endpoints

import cats.data.EitherT
import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.api.documentation.RegisterEndpoint
import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.logic.{RegisterLogic, VerifyLogic}
import ru.zkerriga.investment.storage.entities.Client


class RegisterServerEndpoint(
  registerLogic: RegisterLogic,
  verifyLogic: VerifyLogic,
  exceptionHandler: ExceptionHandler[Task, EitherT])(implicit s: Scheduler) extends Endpoints[Future] {

  private def authorizeWithoutToken(credentials: UsernamePassword): Future[Either[ExceptionResponse, Client]] =
    exceptionHandler.handleEither(verifyLogic.verifyCredentials(credentials)).runToFuture

  private val register =
    RegisterEndpoint.register
      .serverLogic[Future] { login =>
        exceptionHandler.handle(registerLogic.registerClient(login)).runToFuture
      }

  private val updateToken =
    RegisterEndpoint.updateToken
      .serverLogicPart(authorizeWithoutToken)
      .andThen {
        case (client: Client, token: TinkoffToken) =>
          exceptionHandler.handleEither(registerLogic.updateToken(client, token)).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, Unit, Any, Future]] =
    List(register, updateToken)
}
