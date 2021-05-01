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

  private val testUsername = "username"
  private val testPassword = "pass"
  private val testUsernamePassword = UsernamePassword(testUsername, Some(testPassword))
  private val testLogin = Login(testUsername, testPassword)
  private val testCredentials = BasicHttpCredentials(testUsername, testPassword)

  describe(s"POST $link/register") {
    it("register a new client") {
      (mockServiceApi.registerClient _)
        .expects(testLogin)
        .returns(Task.now(testUsername))

      postLogin(testLogin) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe testUsername
      }
    }
    it("register an existed client") {
      (mockServiceApi.registerClient _)
        .expects(testLogin)
        .returns(Task.raiseError(LoginAlreadyExist(testUsername)))

      postLogin(testLogin) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe
          ExceptionResponse(s"Login `$testUsername` already exist")
      }
    }
  }

  describe(s"PUT $link/update/token") {
    it("must return Unauthorized") {
      Put(s"$api/update/token") ~> route ~> check {
        status shouldEqual StatusCodes.Unauthorized
        header[`WWW-Authenticate`].get.challenges.head shouldEqual
          HttpChallenge("Basic", Some("Enter the registration data"))
      }
    }
    it("invalid authentication") {
      (mockServiceApi.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.raiseError[Client](IncorrectCredentials()))

      putToken(TinkoffToken("_")) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Incorrect login or password")
      }
    }
    val client = Client(1, testUsername, testPassword, None)
    it("valid credentials") {
      val token = TinkoffToken("valid token")
      (mockServiceApi.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(client))
      (mockServiceApi.updateToken _)
        .expects(client, token)
        .returns(Task.now(testUsername))

      putToken(token) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe testUsername
      }
    }
    it("valid credentials with invalid token") {
      val token = TinkoffToken("invalid token")
      (mockServiceApi.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(client))
      (mockServiceApi.updateToken _)
        .expects(client, token)
        .returns(Task.raiseError(InvalidToken()))

      putToken(token) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Invalid token")
      }
    }
  }

  protected val mockServiceApi: ServiceApi = mock[ServiceApi]
}
