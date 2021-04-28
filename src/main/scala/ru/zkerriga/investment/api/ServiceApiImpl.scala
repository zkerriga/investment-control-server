package ru.zkerriga.investment.api

import cats.data.OptionT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword
import ru.zkerriga.investment.{IncorrectCredentials, LoginAlreadyExist}
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.logic.AsyncBcrypt
import ru.zkerriga.investment.storage.{Client, ServerDatabase}


class ServiceApiImpl(bcrypt: AsyncBcrypt) extends ServiceApi {

  def registerClient(login: Login): Task[String] =
    bcrypt.hash(login.password) flatMap { hash =>
      ServerDatabase.registerClient(login.login, hash)
        .map(_ => login.login)
        .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.login)))
    }

  def verifyCredentials(credentials: UsernamePassword): Task[Client] =
    (for {
      pass   <- OptionT.fromOption[Task](credentials.password)
      client <- OptionT(ServerDatabase.findClientByLogin(credentials.username))
      result <- OptionT.liftF(bcrypt.verify(pass, client.passwordHash))
      if result
    } yield client)
      .getOrElseF(Task.raiseError[Client](IncorrectCredentials()))

  def updateToken(client: Client, token: TinkoffToken): Task[String] = {
    ServerDatabase.updateClientToken(client.id, token.token)
      .map(_ => client.login)
  }

}
