package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import cats.data.EitherT
import io.circe.Encoder
import monix.eval.Task

import ru.zkerriga.investment.exceptions.OpenApiResponseError
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi._


class TinkoffOpenApiClient(uri: String, startBalance: SandboxSetCurrencyBalanceRequest)
                          (implicit as: ActorSystem) extends OpenApiClient {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  type Fail = OpenApiResponseError

  override def `/sandbox/register`(token: TinkoffToken): EitherT[Task, Fail, TinkoffResponse[Empty]] =
    request[TinkoffResponse[Empty]](POST, "/sandbox/register", token) flatMap
      (_ => `/sandbox/currencies/balance`(token, startBalance))

  private def `/sandbox/currencies/balance`(token: TinkoffToken, balance: SandboxSetCurrencyBalanceRequest): EitherT[Task, Fail, TinkoffResponse[Empty]] =
    request[TinkoffResponse[Empty]](
      POST,
      "/sandbox/currencies/balance",
      token,
      jsonRequestEntity(balance)
    )

  override def `/market/stocks`(token: TinkoffToken): EitherT[Task, Fail, TinkoffResponse[Stocks]] =
    request[TinkoffResponse[Stocks]](GET, "/market/stocks", token)

  override def `/orders/market-order`(token: TinkoffToken, figi: String, marketOrder: MarketOrderRequest): EitherT[Task, Fail, TinkoffResponse[PlacedMarketOrder]] =
    request[TinkoffResponse[PlacedMarketOrder]](
      POST,
      s"/orders/market-order?figi=$figi",
      token,
      jsonRequestEntity(marketOrder)
    )

  override def `/market/orderbook`(token: TinkoffToken, figi: String): EitherT[Task, Fail, TinkoffResponse[OrderBook]] =
    request[TinkoffResponse[OrderBook]](GET, s"/market/orderbook?figi=$figi&depth=20", token)

  private def jsonRequestEntity[A](entity: A)(implicit encoder: Encoder[A]): RequestEntity =
    HttpEntity(ContentTypes.`application/json`, entity.asJson.noSpaces)

  private def request[U: FromResponseUnmarshaller](
      method: HttpMethod,
      path: String,
      token: TinkoffToken,
      entity: RequestEntity = HttpEntity.Empty
    ): EitherT[Task, Fail, U] = EitherT(Task.deferFutureAction { implicit scheduler =>

    Http().singleRequest(
      HttpRequest(
        method = method,
        entity = entity,
        uri = s"$uri$path"
      ).addCredentials(HttpCredentials.createOAuth2BearerToken(token.token))
    ).flatMap { response =>
      Unmarshal(response).to[U].map(Right.apply)
        .recover { case _ => Left(OpenApiResponseError(path)) }
    }
  })
}
