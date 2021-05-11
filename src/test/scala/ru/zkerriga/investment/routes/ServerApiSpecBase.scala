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
import ru.zkerriga.investment.{IncorrectCredentials, InvalidToken, LoginAlreadyExist, NotEnoughBalance, PageNotFound}
import ru.zkerriga.investment.api.ExceptionResponse
import ru.zkerriga.investment.base.ServerConfiguration
import ru.zkerriga.investment.entities.openapi._
import ru.zkerriga.investment.entities._
import ru.zkerriga.investment.logic.{MarketLogic, NotificationLogic, RegisterLogic, VerifyLogic}
import ru.zkerriga.investment.storage.entities.Client


trait ServerApiSpecBase extends AnyFunSpec with ServerConfiguration with ScalatestRouteTest with Matchers with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.syntax._
  import io.circe.generic.auto._

  def route: Route

  private def postLogin(login: Login): HttpRequest =
    Post(s"$api/register", HttpEntity(ContentTypes.`application/json`, login.asJson.noSpaces))

  private def putToken(token: TinkoffToken): HttpRequest =
    Put(s"$api/update/token", HttpEntity(ContentTypes.`application/json`, token.asJson.noSpaces))

  private def getStocks(page: Int, onPage: Int): HttpRequest =
    Get(s"$api/market/stocks?page=$page&onPage=$onPage")

  private def buyStocks(stockOrder: StockOrder): HttpRequest =
    Post(s"$api/orders/market-order/buy", HttpEntity(ContentTypes.`application/json`, stockOrder.asJson.noSpaces))

  private def getNotifications: HttpRequest =
    Get(s"$api/notifications/all")

  private val testUsername = "username"
  private val testPassword = "pass"
  private val testUsernamePassword = UsernamePassword(testUsername, Some(testPassword))
  private val testLogin = Login(testUsername, testPassword)
  private val testCredentials = BasicHttpCredentials(testUsername, testPassword)
  private val testClient = Client(Some(1), testUsername, testPassword, None)
  private val testVerifiedClient = VerifiedClient(1, testUsername, TinkoffToken("valid token"))
  private val testStockOrder = StockOrder("A1B2C3", 1, 90.0, 120.0)
  private val testOrderResponse = PlacedMarketOrder("ae1-12c", "Buy", "Fill", None, None, 1, 1)
  private val testNotifications = Notifications(1, Seq(NotificationMessage(testStockOrder, "Sold")))

  describe(s"POST $link/register") {
    it("register a new client") {
      (mockRegisterLogic.registerClient _)
        .expects(testLogin)
        .returns(Task.now(testUsername))

      postLogin(testLogin) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe testUsername
      }
    }
    it("register an existed client") {
      (mockRegisterLogic.registerClient _)
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
      (mockVerifyLogic.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.raiseError[Client](IncorrectCredentials()))

      putToken(TinkoffToken("_")) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Incorrect login or password")
      }
    }
    it("valid credentials") {
      val token = TinkoffToken("valid token")
      (mockVerifyLogic.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(testClient))
      (mockRegisterLogic.updateToken _)
        .expects(testClient, token)
        .returns(Task.now(testUsername))

      putToken(token) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe testUsername
      }
    }
    it("valid credentials with invalid token") {
      val token = TinkoffToken("invalid token")
      (mockVerifyLogic.verifyCredentials _)
        .expects(testUsernamePassword)
        .returns(Task.now(testClient))
      (mockRegisterLogic.updateToken _)
        .expects(testClient, token)
        .returns(Task.raiseError(InvalidToken()))

      putToken(token) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Invalid token")
      }
    }
  }

  def mockVerify = {
    (mockVerifyLogic.verifyCredentials _)
      .expects(testUsernamePassword)
      .returns(Task.now(testClient))
    (mockVerifyLogic.verifyToken _)
      .expects(testClient)
      .returns(Task.now(testVerifiedClient))
  }

  describe(s"GET $link/market/stocks") {
    it("get normal list") {
      val stocks = Stocks(total = 2, instruments = Seq(
        Stock("A1B2C3", "AAA", "C3B2A1", None, 1, None, "USD", "Company1"),
        Stock("A2B3C4", "BBB", "C4B3A2", None, 1, None, "USD", "Company2"),
      ))
      mockVerify
      (mockMarketLogic.getStocks _)
        .expects(testVerifiedClient, 1, 2)
        .returns(Task.now(stocks))

      getStocks(1, 2) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Stocks] shouldBe stocks
      }
    }
    it("incorrect page") {
      mockVerify
      (mockMarketLogic.getStocks _)
        .expects(testVerifiedClient, 1000, 1000)
        .returns(Task.raiseError(PageNotFound()))

      getStocks(1000, 1000) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Page not found")
      }
    }
  }

  describe(s"POST $link/orders/market-order/buy") {
    it("not enough balance") {
      mockVerify
      (mockMarketLogic.buyStocks _)
        .expects(testVerifiedClient, testStockOrder)
        .returns(Task.raiseError[PlacedMarketOrder](NotEnoughBalance()))

      buyStocks(testStockOrder) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ExceptionResponse] shouldBe ExceptionResponse("Not enough balance")
      }
    }
    it("successful buy") {
      mockVerify
      (mockMarketLogic.buyStocks _)
        .expects(testVerifiedClient, testStockOrder)
        .returns(Task.now(testOrderResponse))

      buyStocks(testStockOrder) ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[PlacedMarketOrder] shouldBe testOrderResponse
      }
    }
  }

  describe(s"GET $link/notifications/all") {
    it("should return notifications") {
      mockVerify
      (mockNotificationLogic.getAllNotifications _)
        .expects(testVerifiedClient)
        .returns(Task.now(testNotifications))

      getNotifications ~> addCredentials(testCredentials) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Notifications] shouldBe testNotifications
      }
    }
  }

  protected val mockRegisterLogic: RegisterLogic = mock[RegisterLogic]
  protected val mockVerifyLogic: VerifyLogic = mock[VerifyLogic]
  protected val mockMarketLogic: MarketLogic = mock[MarketLogic]
  protected val mockNotificationLogic: NotificationLogic = mock[NotificationLogic]
}
