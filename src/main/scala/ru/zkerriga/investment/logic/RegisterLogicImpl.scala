package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.{InternalError, InvalidToken, LoginAlreadyExist}
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.storage.LoginDao
import ru.zkerriga.investment.storage.entities.Client


class RegisterLogicImpl(dao: LoginDao, bcrypt: AsyncBcrypt, openApiClient: OpenApiClient) extends RegisterLogic {

  override def registerClient(login: Login): Task[String] =
    bcrypt.hash(login.password) flatMap { hash =>
      dao.registerClient(login.username, hash)
        .map(_ => login.username)
        .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.username)))
    }

  override def updateToken(client: Client, token: TinkoffToken): Task[String] = {
    lazy val errorTask = Task.raiseError[String](InvalidToken())
    client.id.fold(errorTask) { id =>
      openApiClient.`/sandbox/register`(token)
        .valueOrF(_ => Task.raiseError(InternalError("updateToken")))
        .flatMap(_ => dao.updateClientToken(id, token.token).map(_ => client.login))
    }
  }

}
