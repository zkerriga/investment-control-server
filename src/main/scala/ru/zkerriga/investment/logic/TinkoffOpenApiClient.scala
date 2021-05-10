package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import io.circe.Encoder
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.entities.{StockOrder, TinkoffToken}
import ru.zkerriga.investment.entities.openapi._


class TinkoffOpenApiClient(implicit as: ActorSystem) extends OpenApiClient {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  private lazy val startBalance = SandboxSetCurrencyBalanceRequest("USD", 1000.0)

  def `/sandbox/register`(token: TinkoffToken): Task[TinkoffResponse[Empty]] =
    request[TinkoffResponse[Empty]](POST, "/sandbox/register", token) <*
      `/sandbox/currencies/balance`(token, startBalance)

  private def `/sandbox/currencies/balance`(token: TinkoffToken, balance: SandboxSetCurrencyBalanceRequest): Task[TinkoffResponse[Empty]] =
    request[TinkoffResponse[Empty]](
      POST,
      "/sandbox/currencies/balance",
      token,
      jsonRequestEntity(balance)
    )

  def `/market/stocks`(token: TinkoffToken): Task[TinkoffResponse[Stocks]] =
    request[TinkoffResponse[Stocks]](GET, "/market/stocks", token)

  def `/orders/market-order`(token: TinkoffToken, figi: String, marketOrder: MarketOrderRequest): Task[TinkoffResponse[PlacedMarketOrder]] =
    request[TinkoffResponse[PlacedMarketOrder]](
      POST,
      s"/orders/market-order?figi=$figi",
      token,
      jsonRequestEntity(marketOrder)
    )

  def `/market/orderbook`(token: TinkoffToken, figi: String): Task[TinkoffResponse[OrderBook]] =
    request[TinkoffResponse[OrderBook]](GET, s"/market/orderbook?figi=$figi&depth=20", token)

  private val link = "https://api-invest.tinkoff.ru/openapi/sandbox"

  private def createUri(path: String): Uri = {
    require(path.startsWith("/"), "Path for request must starts with `/`!")
    Uri(s"$link$path")
  }

  private def jsonRequestEntity[A](entity: A)(implicit encoder: Encoder[A]): RequestEntity =
    HttpEntity(ContentTypes.`application/json`, entity.asJson.noSpaces)

  private def request[U: FromResponseUnmarshaller](
      method: HttpMethod,
      path: String,
      token: TinkoffToken,
      entity: RequestEntity = HttpEntity.Empty
    ): Task[U] = Task.deferFutureAction { implicit scheduler =>

    Http().singleRequest(
      HttpRequest(
        method = method,
        entity = entity,
        uri = createUri(path)
      ).addCredentials(HttpCredentials.createOAuth2BearerToken(token.token))
    ).flatMap { response => Unmarshal(response).to[U] }
  }
}
