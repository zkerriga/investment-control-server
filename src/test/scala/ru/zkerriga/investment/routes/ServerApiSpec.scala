package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import monix.execution.Scheduler

import ru.zkerriga.investment.configuration.{Port, ServerConf}
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._


class ServerApiSpec extends ServerApiSpecBase {
  implicit val s: Scheduler = monix.execution.Scheduler.global

  override def route: Route = {
    val exceptionHandler = ExceptionHandlerForTask()
    new ServerRoutesImpl(
      List(
        new RegisterServerEndpoint(mockRegisterLogic, mockVerifyLogic, exceptionHandler),
        new MarketServerEndpoint(mockVerifyLogic, mockMarketLogic, exceptionHandler),
        new OrdersServerEndpoint(mockVerifyLogic, mockMarketLogic, exceptionHandler),
        new NotificationsServerEndpoint(mockVerifyLogic, mockNotificationLogic, exceptionHandler),
      ),
      ServerConf(host = "localhost", port = Port(8080), useHttps = false)
    ).routes
  }

}
