package ru.zkerriga.investment.api

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.entities.VerifiedClient
import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.api.documentation.MarketEndpoint


class MarketServerEndpoint(serviceApi: ServiceLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] {

  private def authorize(credentials: UsernamePassword): Future[Either[ExceptionResponse, VerifiedClient]] =
    exceptionHandler.handle(
      serviceApi.verifyCredentials(credentials) flatMap { client =>
        serviceApi.verifyToken(client)
      }
    ).runToFuture

  private val stocks =
    MarketEndpoint.stocks
      .serverLogicPart(authorize)
      .andThen {
        case (client, (page, onPage)) =>
          exceptionHandler.handle(serviceApi.getStocks(client, page, onPage)).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, Stocks, Any, Future]] =
    List(stocks)
}