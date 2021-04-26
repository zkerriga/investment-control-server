package ru.zkerriga.investment.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory

import ru.zkerriga.investment.api.ServiceApi
import ru.zkerriga.investment.entities.Login

trait ServerApiSpecBase extends AnyFunSpec with ScalatestRouteTest with Matchers with MockFactory {

//  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  import io.circe.syntax._

  def route: Route

  private val api = "/api/v1/investment"

  describe(s"POST http://localhost:port$api/register") {

    it("register new client") {
      val login = Login("login1", "pass1")

      Post(s"$api/register", HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces)) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe login.login
      }
    }

  }

  protected val mockServiceApi: ServiceApi = mock[ServiceApi]

}
