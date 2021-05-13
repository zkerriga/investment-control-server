package ru.zkerriga.investment.logic

import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.{TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.exceptions.{IncorrectCredentials, TokenDoesNotExist}
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.utils.{AsyncBcrypt, AsyncBcryptImpl}


class VerifyLogicImplTest extends AnyFlatSpec with Matchers with MockFactory {

  import monix.execution.Scheduler.Implicits.global

  private lazy val bcrypt: AsyncBcrypt = new AsyncBcryptImpl

  def suite: (FakeLoginDao, VerifyLogicImpl) = {
    val fakeDb = new FakeLoginDao
    val logic = new VerifyLogicImpl(fakeDb, bcrypt)
    (fakeDb, logic)
  }

  private lazy val client = Client(Some(0), "username", "pass", None)

  behavior of "findClientInDb"

  it should "get existed client" in {
    val (fakeDb, logic) = suite

    fakeDb.clientsTable.addOne(0L -> client)
    logic.findClientInDb(client.login).value.runSyncUnsafe() shouldEqual
      Right(client)
  }
  it should "return Incorrect credentials error" in {
    val (_, logic) = suite

    logic.findClientInDb("not exist").value.runSyncUnsafe() shouldBe
      Left(Right(IncorrectCredentials()))
  }

  behavior of "verifyPassword"

  private val mockBcrypt: AsyncBcrypt = mock[AsyncBcrypt]
  private val logicMock = new VerifyLogicImpl(new FakeLoginDao, mockBcrypt)

  it should "return IncorrectCredentials" in {
    (mockBcrypt.verify _)
      .expects("password", client.passwordHash)
      .returns(Task.now(false))

    logicMock.verityPassword(client, "password").value.runSyncUnsafe() shouldBe
      Left(IncorrectCredentials())
  }
  it should "return a client" in {
    (mockBcrypt.verify _)
      .expects("password", client.passwordHash)
      .returns(Task.now(true))

    logicMock.verityPassword(client, "password").value.runSyncUnsafe() shouldEqual
      Right(client)
  }

  behavior of "verifyCredentials"

  it should "return IncorrectCredentials with empty pass or client does not exist in db" in {
    val (_, logic) = suite
    val credentials = UsernamePassword("username", None)

    logic.verifyCredentials(credentials).value.runSyncUnsafe() shouldBe
      Left(Right(IncorrectCredentials()))

    logic.verifyCredentials(UsernamePassword("username", Some("pass"))).value.runSyncUnsafe() shouldBe
      Left(Right(IncorrectCredentials()))
  }
  it should "return normal client" in {
    val fakeDb = new FakeLoginDao
    val logic = new VerifyLogicImpl(fakeDb, mockBcrypt)
    val client1 = client.copy(token = Some("token"))

    (mockBcrypt.verify _)
      .expects("pass", "pass")
      .returns(Task.now(true))

    fakeDb.clientsTable.addOne(0L -> client1)
    logic.verifyCredentials(UsernamePassword("username", Some("pass"))).value.runSyncUnsafe() shouldEqual
      Right(client1)
  }

  behavior of "verifyToken"

  it should "return client if id and token exists" in {
    val client1 = Client(Some(1), "username", "", Some("token"))

    logicMock.verifyToken(client1).value.runSyncUnsafe() shouldBe
      Right(VerifiedClient(1, client1.login, TinkoffToken("token")))
  }
  it should "return TokenDoesNotExist error if token or id is empty" in {
    val client1 = Client(Some(1), "username", "", None)
    val client2 = Client(None, "username", "", Some("token"))

    logicMock.verifyToken(client1).value.runSyncUnsafe() shouldBe
      Left(TokenDoesNotExist())
    logicMock.verifyToken(client2).value.runSyncUnsafe() shouldBe
      Left(TokenDoesNotExist())
  }
}
