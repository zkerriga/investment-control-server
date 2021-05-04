package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._


class TinkoffOpenApiClient(implicit as: ActorSystem, s: Scheduler) extends OpenApiClient {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  private lazy val startBalance = SandboxSetCurrencyBalanceRequest("USD", 1000.0)

  override def `/sandbox/register`(token: TinkoffToken): Task[Register] =
    request[Register](POST, "/sandbox/register", token) <*
      `/sandbox/currencies/balance`(token, startBalance)

  private def `/sandbox/currencies/balance`(token: TinkoffToken, balance: SandboxSetCurrencyBalanceRequest): Task[Register] =
    request[Register](
      POST,
      "/sandbox/currencies/balance",
      token,
      HttpEntity(ContentTypes.`application/json`, balance.asJson.noSpaces)
    )

  override def `/market/stocks`(token: TinkoffToken): Task[TinkoffResponse[Stocks]] =
    request[TinkoffResponse[Stocks]](GET, "/market/stocks", token)


  private val link = "https://api-invest.tinkoff.ru/openapi/sandbox"

  private def createUri(path: String): Uri = {
    require(path.startsWith("/"), "Path for request must starts with `/`!")
    Uri(s"$link$path")
  }

  private def request[U: FromResponseUnmarshaller](
      method: HttpMethod,
      path: String,
      token: TinkoffToken,
      entity: RequestEntity = HttpEntity.Empty): Task[U] = Task.fromFuture{

    Http().singleRequest(
      HttpRequest(
        method = method,
        entity = entity,
        uri = createUri(path)
      ).addCredentials(HttpCredentials.createOAuth2BearerToken(token.token))
    ).flatMap { response => Unmarshal(response).to[U] }
  }
}
