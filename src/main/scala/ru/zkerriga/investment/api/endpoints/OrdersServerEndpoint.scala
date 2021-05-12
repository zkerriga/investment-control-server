package ru.zkerriga.investment.api.endpoints

import cats.data.EitherT
import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.logic.{MarketLogic, VerifyLogic}
import ru.zkerriga.investment.api.documentation.OrdersEndpoint
import ru.zkerriga.investment.entities.openapi.PlacedMarketOrder


class OrdersServerEndpoint(
  verifyLogic: VerifyLogic,
  marketLogic: MarketLogic,
  exceptionHandler: ExceptionHandler[Task, EitherT])(implicit s: Scheduler) extends Endpoints[Future] with Authentication {

  private val buyStocks =
    OrdersEndpoint.marketOrder
      .serverLogicPart(authorizeF(verifyLogic, exceptionHandler))
      .andThen {
        case (client, stockOrder) => exceptionHandler.handleEither(
          marketLogic.buyStocks(client, stockOrder)
        ).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, ExceptionResponse, PlacedMarketOrder, Any, Future]] =
    List(buyStocks)
}
