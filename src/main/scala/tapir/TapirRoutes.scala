package com.zkerriga.server
package tapir

import monix.eval.Task
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import domain.Login
import logic.ServiceLogic


class TapirRoutes(serviceLogic: ServiceLogic) extends TapirSupport {

  import sttp.tapir._

  private val apiEndpoint =
    endpoint.in("api" / "v1" / "investment")

  val register: ServerEndpoint[Login, Unit, Int, Any, Task] =
    apiEndpoint.post
      .description("Registering a new client")
      .in("login")
      .in(jsonBody[Login])
      .out(jsonBody[Int])
      .serverLogic[Task] { login =>
        serviceLogic.registerClient(login)
          .onErrorRecover{ case _ => 0}
          .map(Right.apply)
      }

  val all = List(register)

}
