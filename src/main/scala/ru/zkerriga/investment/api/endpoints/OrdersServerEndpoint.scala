package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.api.documentation.OrdersEndpoint
import ru.zkerriga.investment.entities.openapi.PlacedMarketOrder


class OrdersServerEndpoint(serviceLogic: ServiceLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] with Authentication {

  private val buyStocks =
    OrdersEndpoint.marketOrder
      .serverLogicPart(authorizeF(serviceLogic, exceptionHandler))
      .andThen {
        case (client, stockOrder) => exceptionHandler.handle(
          serviceLogic.buyStocks(client, stockOrder)
        ).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, PlacedMarketOrder, Any, Future]] =
    List(buyStocks)
}
