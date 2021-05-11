package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import io.circe.Encoder
import monix.eval.Task

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._


class TinkoffOpenApiClient(uri: String, startBalance: SandboxSetCurrencyBalanceRequest)
                          (implicit as: ActorSystem) extends OpenApiClient {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

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
        uri = s"$uri$path"
      ).addCredentials(HttpCredentials.createOAuth2BearerToken(token.token))
    ).flatMap { response => Unmarshal(response).to[U] }
  }
}
