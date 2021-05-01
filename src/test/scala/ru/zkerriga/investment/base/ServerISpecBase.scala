package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import scala.concurrent.Future

import ru.zkerriga.investment.ServerConfiguration
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

  private def registerClient(login: Login): Future[String] =
    SimpleHttpClient.post[String](
      Uri(s"$link/register"),
      HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces)
    )

  private def updateToken(credentials: Login, token: TinkoffToken): Future[String] =
    SimpleHttpClient.put[String](
      Uri(s"$link/update/token"),
      HttpEntity(ContentTypes.`application/json`, token.asJson.noSpaces),
      BasicHttpCredentials(credentials.username, credentials.password)
    )

  private val logins: LazyList[Login] = {
    val pattern = "([a-z]+)(\\d+)".r
    LazyList.iterate(Login("username1", "pass1")) {
      case Login(pattern(name, n), pattern(pass, _)) =>
        Login(s"$name${n + 1}", s"$pass${n + 1}")
    }
  }
}
