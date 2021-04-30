package ru.zkerriga.investment.routes

import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import monix.execution.Scheduler
import scala.concurrent.Future
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.EndpointInput.WWWAuthenticate

import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.api.{ExceptionResponse, ServiceApi}
import ru.zkerriga.investment.entities.openapi.Stock
import ru.zkerriga.investment.storage.Client


class TapirRoutes(serviceApi: ServiceApi)(implicit s: Scheduler) extends TapirSupport {

  import sttp.tapir._

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

  val getStocks: ServerEndpoint[(UsernamePassword, (Int, Int)), ExceptionResponse, Seq[Stock], Any, Future] =
    authWithTokenEndpoint.get
      .description("Get a list of shares on the stock exchange")
      .in("market" / "stocks")
      .in(
        query[Int]("page").default(1).description("Page with stocks") and
        query[Int]("onPage").default(20).description("So many stocks will be on one page")
      )
      .out(jsonBody[Seq[Stock]])
      .serverLogic {
          case (client, (page, onPage)) =>
            handleErrors(serviceApi.getStocks(client, page, onPage)).runToFuture
      }

  val updateToken: ServerEndpoint[(UsernamePassword, TinkoffToken), ExceptionResponse, String, Any, Future] =
    authEndpoint.put
      .description("Token Update for Tinkoff Investments")
      .in("update" / "token")
      .in(jsonBody[TinkoffToken])
      .out(jsonBody[String].description("Returns the login in case of successful registration"))
      .serverLogic {
        case (client: Client, token: TinkoffToken) =>
          handleErrors(serviceApi.updateToken(client, token)).runToFuture
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

  val all = List(register, updateToken, getStocks)
}
