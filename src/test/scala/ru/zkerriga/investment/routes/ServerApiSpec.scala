package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import monix.execution.Scheduler


class ServerApiSpec extends ServerApiSpecBase {
  implicit val s: Scheduler = monix.execution.Scheduler.global

  override def route: Route =
    new TapirRoutes(mockServiceApi).routes

}
