package ru.zkerriga.investment.routes

import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

import monix.execution.Scheduler
import scala.concurrent.Future

import ru.zkerriga.investment.entities.Login
import ru.zkerriga.investment.api.ServiceApi


class TapirRoutes(serviceApi: ServiceApi)(implicit s: Scheduler) extends TapirSupport {

  import sttp.tapir._

  private val apiEndpoint =
    endpoint.in("api" / "v1" / "investment")

  val register =
    apiEndpoint.post
      .description("Registering a new client")
      .in("login")
      .in(jsonBody[Login])
      .out(jsonBody[Int])
      .serverLogic[Future] { login =>
        serviceApi.registerClient(login)
          .onErrorRecover{ case _ => 0 }
          .map(Right.apply).runToFuture
      }

  val all = List(register)
}
