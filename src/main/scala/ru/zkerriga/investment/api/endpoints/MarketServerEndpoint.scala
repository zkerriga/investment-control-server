package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.api.documentation.MarketEndpoint
import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}


class MarketServerEndpoint(serviceApi: ServiceLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] with Authentication {

  private val stocks =
    MarketEndpoint.stocks
      .serverLogicPart(authorizeF(serviceApi, exceptionHandler))
      .andThen {
        case (client, (page, onPage)) =>
          exceptionHandler.handle(serviceApi.getStocks(client, page, onPage)).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, Stocks, Any, Future]] =
    List(stocks)
}
