package ru.zkerriga.investment.tapir

import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import ru.zkerriga.investment.domain.Login
import ru.zkerriga.investment.logic.ServiceLogic

import scala.concurrent.{ExecutionContext, Future}


class TapirRoutes(serviceLogic: ServiceLogic)(implicit ec: ExecutionContext) extends TapirSupport {

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
        serviceLogic.registerClient(login)
          .recover{ case _ => 0 }
          .map(Right.apply)
      }

  val all = List(register)

}
