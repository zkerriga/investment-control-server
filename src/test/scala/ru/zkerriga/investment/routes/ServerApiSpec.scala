package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import monix.execution.Scheduler
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import ru.zkerriga.investment.api.ServiceApiImpl

class ServerApiSpec extends ServerApiSpecBase {
  implicit val s: Scheduler = monix.execution.Scheduler.global

  override def route: Route =
    AkkaHttpServerInterpreter.toRoute(
      new TapirRoutes(new ServiceApiImpl).all
    )

}
