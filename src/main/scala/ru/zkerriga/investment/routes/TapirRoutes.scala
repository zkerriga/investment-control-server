package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import monix.execution.Scheduler

import scala.concurrent.Future
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.EndpointInput.WWWAuthenticate
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.api.ExceptionResponse
import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.storage.Client


class TapirRoutes(serviceApi: ServiceLogic)(implicit s: Scheduler) extends ServerRoutes with TapirSupport {

  import sttp.tapir._
  import sttp.tapir.openapi._

  private val apiEndpoint =
    endpoint.in("api" / "v1" / "investment")

  private val wwwAuth = WWWAuthenticate.basic("Enter the registration data")
  private val preAuthEndpoint =
    apiEndpoint
      .in(auth.basic[UsernamePassword](wwwAuth))
      .errorOut(jsonBody[ExceptionResponse].description("If authentication failed"))

  private val authEndpoint =
    preAuthEndpoint
      .serverLogicForCurrent[Client, Future] { credentials =>
        handleErrors(serviceApi.verifyCredentials(credentials)).runToFuture
      }

  private val authWithTokenEndpoint =
    preAuthEndpoint
      .serverLogicForCurrent[VerifiedClient, Future] { credentials =>
        handleErrors(
          for {
            client <- serviceApi.verifyCredentials(credentials)
            verifiedClient <- serviceApi.verifyToken(client)
          } yield verifiedClient
        ).runToFuture
      }

  val getStocks: ServerEndpoint[(UsernamePassword, (Int, Int)), ExceptionResponse, Stocks, Any, Future] =
    authWithTokenEndpoint.get
      .tag("market")
      .summary("Get a list of stocks")
      .description("The request goes to Tinkoff-OpenAPI and gets a part of the list of available stocks")
      .in("market" / "stocks")
      .in(
        query[Int]("page").default(1).description("Page with stocks") and
        query[Int]("onPage").default(20).description("So many stocks will be on one page")
      )
      .out(jsonBody[Stocks])
      .serverLogic {
          case (client, (page, onPage)) =>
            handleErrors(serviceApi.getStocks(client, page, onPage)).runToFuture
      }

  val updateToken: ServerEndpoint[(UsernamePassword, TinkoffToken), ExceptionResponse, String, Any, Future] =
    authEndpoint.put
      .tag("registration")
      .summary("Updates the token")
      .description("Checks the validity of the token from Tinkoff-OpenAPI and enters it in the client data")
      .in("update" / "token")
      .in(jsonBody[TinkoffToken])
      .out(jsonBody[String].description("Returns the username in case of successful registration"))
      .serverLogic {
        case (client: Client, token: TinkoffToken) =>
          handleErrors(serviceApi.updateToken(client, token)).runToFuture
      }

  val register: ServerEndpoint[Login, ExceptionResponse, String, Any, Future] =
    apiEndpoint.post
      .tag("registration")
      .summary("Registers a client")
      .description("Checks if the username is free and registers a new client")
      .in("register")
      .in(jsonBody[Login])
      .out(jsonBody[String].description("Returns the username in case of successful registration"))
      .errorOut(jsonBody[ExceptionResponse].description("The login may be busy"))
      .serverLogic[Future] { login =>
        handleErrors(serviceApi.registerClient(login)).runToFuture
      }

  private val all = List(register, updateToken, getStocks)

  override val openapi: OpenAPI = OpenAPIDocsInterpreter.serverEndpointsToOpenAPI(
    all,
    Info(
      title = "The Investment Control Server",
      version = "0.0.1",
      description = Some("Server for working with StopLoss and TakeProfit strategies via Tinkoff OpenAPI"),
      contact = Some(Contact(
        name = Some("Daniil Eletskii"),
        email = None,
        url = Some("https://github.com/zkerriga")
      ))
    )
  )

  override val routes: Route = AkkaHttpServerInterpreter.toRoute(all)
}
