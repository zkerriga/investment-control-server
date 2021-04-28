package ru.zkerriga.investment.routes

import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import monix.execution.Scheduler

import scala.concurrent.Future
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint

import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.api.{ExceptionResponse, ServiceApi}
import ru.zkerriga.investment.storage.Client


class TapirRoutes(serviceApi: ServiceApi)(implicit s: Scheduler) extends TapirSupport {

  import sttp.tapir._

  private val apiEndpoint =
    endpoint.in("api" / "v1" / "investment")

  private val authEndpoint =
    apiEndpoint
      .in(auth.basic[UsernamePassword]())
      .errorOut(jsonBody[ExceptionResponse].description("If authentication failed"))
      .serverLogicForCurrent[Client, Future] { credentials =>
        handleErrors(serviceApi.verifyCredentials(credentials)).runToFuture
      }

  val updateToken: ServerEndpoint[(UsernamePassword, TinkoffToken), ExceptionResponse, String, Any, Future] =
    authEndpoint.put
      .description("Token Update for Tinkoff Investments")
      .in("update" / "token")
      .in(jsonBody[TinkoffToken])
      .out(jsonBody[String].description("Returns the login in case of successful registration"))
      .serverLogic {
        case (client: Client, token: TinkoffToken) =>
          serviceApi.updateToken(client, token).map(Right.apply).runToFuture
      }

  val register: ServerEndpoint[Login, ExceptionResponse, String, Any, Future] =
    apiEndpoint.post
      .description("Registering a new client")
      .in("register")
      .in(jsonBody[Login])
      .out(jsonBody[String].description("Returns the login in case of successful registration"))
      .errorOut(jsonBody[ExceptionResponse].description("The login may be busy"))
      .serverLogic[Future] { login =>
        handleErrors(serviceApi.registerClient(login)).runToFuture
      }

  val all = List(register, updateToken)
}
