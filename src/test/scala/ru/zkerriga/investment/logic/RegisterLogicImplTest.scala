package ru.zkerriga.investment.logic

import cats.data.EitherT
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import ru.zkerriga.investment.TestOpenApiClient
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.exceptions.{InvalidToken, OpenApiResponseError, ProgrammedError, UsernameAlreadyExist}
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.utils.{AsyncBcrypt, AsyncBcryptImpl}


class RegisterLogicImplTest extends AnyFlatSpec with Matchers with MockFactory {

  import monix.execution.Scheduler.Implicits.global

  private val bcrypt: AsyncBcrypt = new AsyncBcryptImpl

  def suite: (FakeLoginDao, RegisterLogicImpl) = {
    val fakeDb = new FakeLoginDao
    val logic = new RegisterLogicImpl(fakeDb, bcrypt, TestOpenApiClient)
    (fakeDb, logic)
  }

  behavior of "registerClient"

  private val client = Client(Some(0), "username", "pass", None)

  it should "return error if client already exist" in {
    val (fakeDb, logic) = suite

    fakeDb.clientsTable.addOne(0L -> client)
    logic.registerClient(Login("username", "pass")).value.runSyncUnsafe() shouldEqual
      Left(UsernameAlreadyExist("username"))
    fakeDb.clientsTable.readOnlySnapshot.toSeq shouldBe
      Seq(0L -> client)
  }
  it should "return unit" in {
    val (fakeDb, logic) = suite

    logic.registerClient(Login("username", "pass")).value.runSyncUnsafe() shouldEqual
      Right(())
    val res = fakeDb.clientsTable.readOnlySnapshot.toSeq
    res.size shouldBe 1
    bcrypt.verify("pass", res.head._2.passwordHash).runSyncUnsafe() shouldBe true
  }

  behavior of "updateToken"

  it should "update token" in {
    val (fakeDb, logic) = suite
    val token = TinkoffToken("token")

    fakeDb.clientsTable.addOne(0L -> client)
    logic.updateToken(client, token).value.runSyncUnsafe() shouldEqual
      Right(())

    fakeDb.clientsTable.readOnlySnapshot().toSeq shouldEqual
      Seq(0L -> client.copy(token = Some(token.token)))
  }
  it should "get a Programmer error" in {
    val (fakeDb, logic) = suite

    logic.updateToken(Client(None, "u", "p", None), TinkoffToken("")).value.runSyncUnsafe() shouldBe
      Left(Left(ProgrammedError("Client without id")))

    fakeDb.clientsTable.isEmpty shouldBe true
  }
  it should "get a Invalid token exception" in {
    val mockOpenApi: OpenApiClient = mock[OpenApiClient]
    val logic = new RegisterLogicImpl(new FakeLoginDao, bcrypt, mockOpenApi)
    val token = TinkoffToken("token")

    (mockOpenApi.`/sandbox/register` _)
      .expects(token)
      .returns(EitherT.leftT(OpenApiResponseError("/sandbox/register")))

    logic.updateToken(client, token).value.runSyncUnsafe() shouldBe
      Left(Right(InvalidToken()))
  }

}
