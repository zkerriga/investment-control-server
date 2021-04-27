package ru.zkerriga.investment.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import monix.eval.Task
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory

import ru.zkerriga.investment.{LoginAlreadyExist, ServerConfiguration}
import ru.zkerriga.investment.api.{ExceptionResponse, ServiceApi}
import ru.zkerriga.investment.entities.Login


trait ServerApiSpecBase extends AnyFunSpec with ServerConfiguration with ScalatestRouteTest with Matchers with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  def route: Route

  private def postLogin(login: Login): HttpRequest =
    Post(s"$api/register", HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces))

  describe(s"POST $link/register") {

    it("register a new client") {
      val login = Login("login1", "pass1")
      (mockServiceApi.registerClient _)
        .expects(login)
        .returns(Task.now(login.login))

      postLogin(login) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe login.login
      }
    }
    it("register an existed client") {
      val login = Login("login1", "pass1")
      (mockServiceApi.registerClient _)
        .expects(login)
        .returns(Task.raiseError(LoginAlreadyExist(login.login)))

      postLogin(login) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse(s"Login `${login.login}` already exist")
      }

    }

  }

  protected val mockServiceApi: ServiceApi = mock[ServiceApi]
}
