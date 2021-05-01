package ru.zkerriga.investment.api

import cats.data.OptionT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.openapi.Stock
import ru.zkerriga.investment.{IncorrectCredentials, InvalidToken, LoginAlreadyExist, PageNotFound, TokenDoesNotExist}
import ru.zkerriga.investment.entities.{Login, TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.logic.{AsyncBcrypt, OpenApiClient}
import ru.zkerriga.investment.storage.{Client, ServerDatabase}


class ServiceApiImpl(bcrypt: AsyncBcrypt, openApiClient: OpenApiClient) extends ServiceApi {

  def registerClient(login: Login): Task[String] =
    bcrypt.hash(login.password) flatMap { hash =>
      ServerDatabase.registerClient(login.username, hash)
        .map(_ => login.username)
        .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.username)))
    }

  def verifyCredentials(credentials: UsernamePassword): Task[Client] =
    (for {
      pass   <- OptionT.fromOption[Task](credentials.password)
      client <- OptionT(ServerDatabase.findClientByLogin(credentials.username))
      result <- OptionT.liftF(bcrypt.verify(pass, client.passwordHash))
      if result
    } yield client)
      .getOrElseF(Task.raiseError[Client](IncorrectCredentials()))

  def verifyToken(client: Client): Task[VerifiedClient] =
    client.token
      .fold(Task.raiseError[VerifiedClient](TokenDoesNotExist())){ token =>
        Task.now(VerifiedClient.fromClient(client, TinkoffToken(token)))
      }

  def updateToken(client: Client, token: TinkoffToken): Task[String] = {
    openApiClient.`/sandbox/register`(token)
      .redeemWith(
        _ => Task.raiseError(InvalidToken()),
        _ => ServerDatabase.updateClientToken(client.id, token.token)
          .map(_ => client.login)
      )
  }

  def getStocks(client: VerifiedClient, page: Int, onPage: Int): Task[Seq[Stock]] = {
    openApiClient.`/market/stocks`(client.token) flatMap { response =>
      val result = response.payload.instruments
        .slice(onPage * (page - 1), onPage * page)

      if (result.isEmpty) Task.raiseError(PageNotFound())
      else Task.now(result)
    }
  }
}
