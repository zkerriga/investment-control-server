package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, RequestEntity, Uri}
import io.circe.Encoder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import scala.concurrent.Future

import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.entities.{Login, TinkoffToken}


trait ServerISpecBase extends AsyncFunSuite with ServerConfiguration with BeforeAndAfterAll {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  implicit def as: ActorSystem

  test("registrations") {
    val successful = for {
      username1 <- registerClient(logins(1))
      username2 <- registerClient(logins(2))
    } yield {
      assert(username1 === logins(1).username)
      assert(username2 === logins(2).username)
    }
    successful.map{ _ => registerClient(logins(1)) }
      .map(_ => fail())
      .recover{ case _ => succeed }
  }

  test("register and add token") {
    for {
      username1 <- registerClient(logins(3))
      fail <- updateToken(logins(3), TinkoffToken("invalid token")).recover(_ => "fail")
    } yield {
      assert(username1 === logins(3).username)
      assert(fail === "fail")
    }
  }

  ignore("get stocks") {
    val login = logins(4)
    for {
      _ <- registerClient(login)
      _ <- updateToken(login, TinkoffToken("VALID TOKEN???")) /* todo: add mock for openApi */
      stocks1 <- getStocks(login, Map("page" -> 1, "onPage" -> 2))
      stocks2 <- getStocks(login, Map("onPage" -> 2))
      stocks3 <- getStocks(login, Map.empty)
    } yield {
      assert(stocks1.instruments.size === 2)
      assert(stocks2.instruments.size === 2)
      assert(stocks3.instruments.size === 20)
    }
  }

  private def registerClient(login: Login): Future[String] =
    SimpleHttpClient.post[String](
      Uri(s"$link/register"),
      toEntity(login)
    )

  private def updateToken(login: Login, token: TinkoffToken): Future[String] =
    SimpleHttpClient.put[String](
      Uri(s"$link/update/token"),
      toEntity(token),
      toCredentials(login)
    )

  private def getStocks(login: Login, queryArgs: Map[String, Int]): Future[Stocks] =
    SimpleHttpClient.get[Stocks](
      uri = Uri(s"$link/market/stocks" ++ queryArgs.map{ case (s, i) => s"$s=$i" }.mkString("?", "&", "")),
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
