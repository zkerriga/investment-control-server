package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import monix.execution.Scheduler
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import ru.zkerriga.investment.entities.Login

import scala.concurrent.Future

trait ServerISpecBase extends AsyncFunSuite with BeforeAndAfterAll {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  implicit def as: ActorSystem
  implicit def s: Scheduler

  def interface: String = "localhost"
  def port: Int = 8080

  private val url = s"http://$interface:$port"
  private val api = "/api/v1/investment"

  test("registrations") {
    for {
      login1 <- registerClient(sampleLogin1)
      login2 <- registerClient(sampleLogin2)
    } yield {
      assert(login1 === sampleLogin1.login)
      assert(login2 === sampleLogin2.login)
    }
  }

  private def registerClient(login: Login): Future[String] =
    SimpleHttpClient.post[String](
      Uri(s"$url/$api/register"),
      HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces)
    )

  private val sampleLogin1 =  Login("login1", "pass1")
  private val sampleLogin2 =  Login("login2", "pass2")
  private val sampleLogin3 =  Login("login3", "pass3")

}
