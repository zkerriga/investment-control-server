package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.{HttpMethod, HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi.{Register, Stocks, TinkoffResponse}


class TinkoffOpenApiClient(implicit as: ActorSystem, s: Scheduler) extends OpenApiClient {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override def `/sandbox/register`(token: TinkoffToken): Task[Register] =
    request[Register](POST, "/sandbox/register", token)

  override def `/market/stocks`(token: TinkoffToken): Task[TinkoffResponse[Stocks]] =
    request[TinkoffResponse[Stocks]](GET, "/market/stocks", token)


  private val link = "https://api-invest.tinkoff.ru/openapi/sandbox"

  private def createUri(path: String): Uri = {
    require(path.startsWith("/"), "Path for request must starts with `/`!")
    Uri(s"$link$path")
  }

  private def request[U: FromResponseUnmarshaller](method: HttpMethod, path: String, token: TinkoffToken): Task[U] = Task.fromFuture{
    Http().singleRequest(
      HttpRequest(
        method = method,
        uri = createUri(path)
      ).addCredentials(HttpCredentials.createOAuth2BearerToken(token.token))
    ).flatMap { response => Unmarshal(response).to[U] }
  }
}
