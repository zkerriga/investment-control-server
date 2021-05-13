package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, RequestEntity, Uri}
import io.circe.Encoder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import scala.concurrent.Future

import ru.zkerriga.investment.entities.openapi.{PlacedMarketOrder, Stock, Stocks}
import ru.zkerriga.investment.entities.{Login, Notifications, StockOrder, TinkoffToken}


trait ServerISpecBase extends AsyncFunSuite with ServerConfiguration with BeforeAndAfterAll {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._
  import io.circe.generic.auto._

  implicit def as: ActorSystem
  def validToken: TinkoffToken

  test("registrations") {
    val successful = for {
      _ <- registerClient(logins(1))
      _ <- registerClient(logins(2))
    } yield {
      succeed
    }
    successful.map{ _ => registerClient(logins(1)) }
      .map(_ => fail())
      .recover{ case _ => succeed }
  }

  test("register and add token") {
    for {
      _ <- registerClient(logins(3))
      _ <- updateToken(logins(3), TinkoffToken("invalid token")).recover(_ => ())
    } yield {
      succeed
    }
  }

  test("get stocks") {
    val login = logins(4)
    for {
      _ <- registerClient(login)
      _ <- updateToken(login, validToken)
      stocks1 <- getStocks(login, Map("page" -> 1, "onPage" -> 2))
      stocks2 <- getStocks(login, Map("onPage" -> 2))
      stocks3 <- getStocks(login, Map.empty)
    } yield {
      assert(stocks1.instruments.size === 2)
      assert(stocks2.instruments.size === 2)
      assert(stocks3.instruments.size === 20)
    }
  }

  test("buy stocks and get notifications") {
    val login = logins(5)
    for {
      _ <- registerClient(login)
      _ <- updateToken(login, validToken)
      stocks <- getStocks(login, Map.empty)
      figi = stocks.instruments.collectFirst{ case Stock(figi, _, _, _, _, _, "USD", _) => figi }
      notifications <- getNotifications(login)
      buyRes <- buyStocks(login, StockOrder(figi = figi.getOrElse(""), lots = 1, stopLoss = 1.0, takeProfit = 1000.0))
    } yield {
      assert(stocks.total > 0)
      assert(figi.nonEmpty)
      assert(buyRes.executedLots === 1 && buyRes.requestedLots === 1)
      assert(notifications.total === 0 && notifications.notifications.isEmpty)
    }
  }

  private def registerClient(login: Login): Future[Unit] =
    SimpleHttpClient.postWithoutCreds[Unit](
      Uri(s"$link/register"),
      toEntity(login)
    )

  private def updateToken(login: Login, token: TinkoffToken): Future[Unit] =
    SimpleHttpClient.put[Unit](
      Uri(s"$link/update/token"),
      toEntity(token),
      toCredentials(login)
    )

  private def getStocks(login: Login, queryArgs: Map[String, Int]): Future[Stocks] =
    SimpleHttpClient.get[Stocks](
      uri = Uri(s"$link/market/stocks" ++ queryArgs.map{ case (s, i) => s"$s=$i" }.mkString("?", "&", "")),
      toCredentials(login)
    )

  private def getNotifications(login: Login): Future[Notifications] =
    SimpleHttpClient.get[Notifications](
      uri = Uri(s"$link/notifications/all"),
      toCredentials(login)
    )

  private def buyStocks(login: Login, stockOrder: StockOrder): Future[PlacedMarketOrder] =
    SimpleHttpClient.post[PlacedMarketOrder](
      uri = Uri(s"$link/orders/market-order/buy"),
      toEntity(stockOrder),
      toCredentials(login)
    )

  private def toCredentials(login: Login): HttpCredentials =
    BasicHttpCredentials(login.username, login.password)

  private def toEntity[A](entity: A)(implicit encoder: Encoder[A]): RequestEntity =
    HttpEntity(ContentTypes.`application/json`, entity.asJson.noSpaces)

  private val logins: LazyList[Login] = {
    val pattern = "([a-z]+)(\\d+)".r
    LazyList.iterate(Login("username1", "pass1")) {
      case Login(pattern(name, n), pattern(pass, _)) =>
        Login(s"$name${n.toInt + 1}", s"$pass${n.toInt + 1}")
    }
  }
}
