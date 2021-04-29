package ru.zkerriga.investment.routes

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenge, `WWW-Authenticate`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import monix.eval.Task
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import ru.zkerriga.investment.{IncorrectCredentials, InvalidToken, LoginAlreadyExist, ServerConfiguration}
import ru.zkerriga.investment.api.{ExceptionResponse, ServiceApi}
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.storage.Client
import sttp.tapir.model.UsernamePassword


trait ServerApiSpecBase extends AnyFunSpec with ServerConfiguration with ScalatestRouteTest with Matchers with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  def route: Route

  private def postLogin(login: Login): HttpRequest =
    Post(s"$api/register", HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces))

  private def putToken(token: TinkoffToken): HttpRequest =
    Put(s"$api/update/token", HttpEntity(ContentTypes.`application/json`, token.asJson.noSpaces))

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

  describe(s"PUT $link/update/token") {
    it("must return Unauthorized") {
      Put(s"$api/update/token") ~> route ~> check {
        status shouldEqual StatusCodes.Unauthorized
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge("Basic", Some("Enter the registration data"))
      }
    }
    it("invalid authentication") {
      val credentials = BasicHttpCredentials("login1", "pass1")
      (mockServiceApi.verifyCredentials _)
        .expects(UsernamePassword("login1", Some("pass1")))
        .returns(Task.raiseError[Client](IncorrectCredentials()))

      putToken(TinkoffToken("_")) ~> addCredentials(credentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Incorrect login or password")
      }
    }
    it("valid credentials") {
      val credentials = BasicHttpCredentials("login1", "pass1")
      val client = Client(1, "login1", "#*#", None)
      val token = TinkoffToken("valid token")
      (mockServiceApi.verifyCredentials _)
        .expects(UsernamePassword(credentials.username, Some(credentials.password)))
        .returns(Task.now(client))
      (mockServiceApi.updateToken _)
        .expects(client, token)
        .returns(Task.now("login1"))

      putToken(token) ~> addCredentials(credentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe "login1"
      }
    }
    it("valid credentials with invalid token") {
      val credentials = BasicHttpCredentials("login1", "pass1")
      val client = Client(1, "login1", "#*#", None)
      val token = TinkoffToken("invalid token")
      (mockServiceApi.verifyCredentials _)
        .expects(UsernamePassword(credentials.username, Some(credentials.password)))
        .returns(Task.now(client))
      (mockServiceApi.updateToken _)
        .expects(client, token)
        .returns(Task.raiseError(InvalidToken()))

      putToken(token) ~> addCredentials(credentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Invalid token")
      }
    }
  }

  protected val mockServiceApi: ServiceApi = mock[ServiceApi]
}
