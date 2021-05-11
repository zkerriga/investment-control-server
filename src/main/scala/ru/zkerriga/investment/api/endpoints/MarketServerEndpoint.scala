package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.logic.{MarketLogic, VerifyLogic}
import ru.zkerriga.investment.api.documentation.MarketEndpoint
import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}


class MarketServerEndpoint(verifyLogic: VerifyLogic, marketLogic: MarketLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] with Authentication {

  private val stocks =
    MarketEndpoint.stocks
      .serverLogicPart(authorizeF(verifyLogic, exceptionHandler))
      .andThen {
        case (client, (page, onPage)) =>
          exceptionHandler.handle(marketLogic.getStocks(client, page, onPage)).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, Stocks, Any, Future]] =
    List(stocks)
}
