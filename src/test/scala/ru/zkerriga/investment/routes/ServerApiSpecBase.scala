package ru.zkerriga.investment.routes

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenge, `WWW-Authenticate`}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import monix.eval.Task
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.{IncorrectCredentials, InvalidToken, LoginAlreadyExist, PageNotFound, ServerConfiguration}
import ru.zkerriga.investment.api.ExceptionResponse
import ru.zkerriga.investment.entities.openapi.{Stock, Stocks}
import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.logic.ServiceLogic
import ru.zkerriga.investment.storage.Client


trait ServerApiSpecBase extends AnyFunSpec with ServerConfiguration with ScalatestRouteTest with Matchers with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._

  def route: Route

  private def postLogin(login: Login): HttpRequest =
    Post(s"$api/register", HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces))

  private def putToken(token: TinkoffToken): HttpRequest =
    Put(s"$api/update/token", HttpEntity(ContentTypes.`application/json`, token.asJson.noSpaces))

  private def getStocks(page: Int, onPage: Int): HttpRequest =
    Get(s"$api/market/stocks?page=$page&onPage=$onPage")

  private val testUsername = "username"
  private val testPassword = "pass"
  private val testUsernamePassword = UsernamePassword(testUsername, Some(testPassword))
  private val testLogin = Login(testUsername, testPassword)
  private val testCredentials = BasicHttpCredentials(testUsername, testPassword)
  private val testClient = Client(1, testUsername, testPassword, None)
  private val testVerifiedClient = VerifiedClient.fromClient(testClient, TinkoffToken("valid token"))

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
    it("valid credentials") {
      val token = TinkoffToken("valid token")
      (mockServiceApi.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(testClient))
      (mockServiceApi.updateToken _)
        .expects(testClient, token)
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
        .returns(Task.now(testClient))
      (mockServiceApi.updateToken _)
        .expects(testClient, token)
        .returns(Task.raiseError(InvalidToken()))

      putToken(token) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Invalid token")
      }
    }
  }

  describe(s"GET $link/market/stocks") {
    def mockVerify = {
      (mockServiceApi.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(testClient))
      (mockServiceApi.verifyToken _)
        .expects(testClient)
        .returns(Task.now(testVerifiedClient))
    }

    it("get normal list") {
      val stocks = Stocks(total = 2, instruments = Seq(
        Stock("A1B2C3", "AAA", "C3B2A1", None, 1, None, "USD", "Company1"),
        Stock("A2B3C4", "BBB", "C4B3A2", None, 1, None, "USD", "Company2"),
      ))
      mockVerify
      (mockServiceApi.getStocks _)
        .expects(testVerifiedClient, 1, 2)
        .returns(Task.now(stocks))

      getStocks(1, 2) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Stocks] shouldBe stocks
      }
    }
    it("incorrect page") {
      mockVerify
      (mockServiceApi.getStocks _)
        .expects(testVerifiedClient, 1000, 1000)
        .returns(Task.raiseError(PageNotFound()))

      getStocks(1000, 1000) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Page not found")
      }
    }
  }

  protected val mockServiceApi: ServiceLogic = mock[ServiceLogic]
}
