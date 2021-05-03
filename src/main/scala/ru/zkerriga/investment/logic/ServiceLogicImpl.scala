package ru.zkerriga.investment.logic

import cats.data.OptionT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.storage.Dao
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment._


class ServiceLogicImpl(bcrypt: AsyncBcrypt, openApiClient: OpenApiClient, dao: Dao) extends ServiceLogic {

  def registerClient(login: Login): Task[String] =
    bcrypt.hash(login.password) flatMap { hash =>
      dao.registerClient(login.username, hash)
        .map(_ => login.username)
        .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.username)))
    }

  def verifyCredentials(credentials: UsernamePassword): Task[Client] =
    (for {
      pass   <- OptionT.fromOption[Task](credentials.password)
      client <- OptionT(dao.findClientByUsername(credentials.username))
      result <- OptionT.liftF(bcrypt.verify(pass, client.passwordHash))
      if result
    } yield client)
      .getOrElseF(Task.raiseError[Client](IncorrectCredentials()))

  def verifyToken(client: Client): Task[VerifiedClient] =
    (for {
      id <- client.id
      token <- client.token
    } yield VerifiedClient(id, client.login, TinkoffToken(token)))
      .fold(Task.raiseError[VerifiedClient](TokenDoesNotExist()))(Task.now)

  def updateToken(client: Client, token: TinkoffToken): Task[String] = {
    lazy val errorTask = Task.raiseError[String](InvalidToken())
    client.id.fold(errorTask) { id =>
      openApiClient.`/sandbox/register`(token)
        .redeemWith(
          _ => errorTask,
          _ => dao.updateClientToken(id, token.token)
            .map(_ => client.login)
        )
    }
  }

  def getStocks(client: VerifiedClient, page: Int, onPage: Int): Task[Stocks] =
    openApiClient.`/market/stocks`(client.token) flatMap { response =>
      val resultStocks = response.payload.instruments
        .slice(onPage * (page - 1), onPage * page)

      if (resultStocks.isEmpty)
        Task.raiseError(PageNotFound())
      else
        Task.now(response.payload.copy(
          total = resultStocks.size,
          instruments = resultStocks
        ))
    }
}
