package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import scala.concurrent.Future

import ru.zkerriga.investment.ServerConfiguration
import ru.zkerriga.investment.entities.Login


trait ServerISpecBase extends AsyncFunSuite with ServerConfiguration with BeforeAndAfterAll {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  implicit def as: ActorSystem

  test("registrations") {
    val successful = for {
      login1 <- registerClient(sampleLogin1)
      login2 <- registerClient(sampleLogin2)
    } yield {
      assert(login1 === sampleLogin1.login)
      assert(login2 === sampleLogin2.login)
    }
    successful.map{ _ => registerClient(sampleLogin1) }
      .map(_ => fail())
      .recover{ case _ => succeed }
  }

  private def registerClient(login: Login): Future[String] =
    SimpleHttpClient.post[String](
      Uri(s"$link/register"),
      HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces)
    )

  private val sampleLogin1 =  Login("login1", "pass1")
  private val sampleLogin2 =  Login("login2", "pass2")
  private val sampleLogin3 =  Login("login3", "pass3")

}