package ru.zkerriga.investment.newroutes

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionResponse, ServiceApi}
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.newroutes.documentation.RegisterEndpoint
import ru.zkerriga.investment.storage.Client


class RegisterServerEndpoint(serviceApi: ServiceApi, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler) extends Endpoints[Future] {

  private def authorizeWithoutToken(credentials: UsernamePassword): Future[Either[ExceptionResponse, Client]] =
    exceptionHandler.handle(serviceApi.verifyCredentials(credentials)).runToFuture

  private val register =
    RegisterEndpoint.register
      .serverLogic[Future] { login =>
        exceptionHandler.handle(serviceApi.registerClient(login)).runToFuture
      }

  private val updateToken =
    RegisterEndpoint.updateToken
      .serverLogicPart(authorizeWithoutToken)
      .andThen {
        case (client: Client, token: TinkoffToken) =>
          exceptionHandler.handle(serviceApi.updateToken(client, token)).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, String, Any, Future]] =
    List(register, updateToken)
}
