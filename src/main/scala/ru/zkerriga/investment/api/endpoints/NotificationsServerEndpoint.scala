package ru.zkerriga.investment.api.endpoints

import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.model.UsernamePassword
import scala.concurrent.Future

import ru.zkerriga.investment.api.{ExceptionHandler, ExceptionResponse}
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.api.documentation.NotificationsEndpoint
import ru.zkerriga.investment.entities.Notifications


class NotificationsServerEndpoint(serviceLogic: ServiceLogic, exceptionHandler: ExceptionHandler[Task])(implicit s: Scheduler)
  extends Endpoints[Future] with Authentication {

  private val getAllNotifications: ServerEndpoint[UsernamePassword, ExceptionResponse, Notifications, Any, Future] =
    NotificationsEndpoint.getAllNotifications
      .serverLogicPart(authorizeF(serviceLogic, exceptionHandler))
      .andThen {
        case (client, _) =>
          serviceLogic.getAllNotifications(client).map(Right.apply).runToFuture
      }

  override def endpoints: List[ServerEndpoint[_, _, _, Any, Future]] =
    List(getAllNotifications)
}
