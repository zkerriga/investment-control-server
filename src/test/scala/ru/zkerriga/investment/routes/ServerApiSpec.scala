package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import monix.execution.Scheduler

import ru.zkerriga.investment.Main
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._


class ServerApiSpec extends ServerApiSpecBase {
  implicit val s: Scheduler = monix.execution.Scheduler.global

  override def route: Route =
    new ServerRoutesImpl(
      List(
        new MarketServerEndpoint(mockServiceApi, ExceptionHandlerForTask()),
        new RegisterServerEndpoint(mockServiceApi, ExceptionHandlerForTask())
      ),
      Main.baseUrl
    ).routes

}
