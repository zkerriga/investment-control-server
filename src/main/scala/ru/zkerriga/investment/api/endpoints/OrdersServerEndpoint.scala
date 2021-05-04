package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.api.ExceptionHandler
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.api.documentation.OrdersEndpoint

class OrdersServerEndpoint(serviceLogic: ServiceLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] with Authentication {

  private val buyStocks =
    OrdersEndpoint.marketOrder
      .serverLogicPart(authorizeF(serviceLogic, exceptionHandler))
      .andThen {
        case (client, stockOrder) => ???
      }


  override def endpoints: List[ServerEndpoint[_, _, _, Any, Future]] = ???
}
