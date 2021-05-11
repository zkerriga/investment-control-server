package ru.zkerriga.investment.logic

import cats.data.OptionT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.{IncorrectCredentials, TokenDoesNotExist}
import ru.zkerriga.investment.entities.{TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.storage.LoginDao
import ru.zkerriga.investment.storage.entities.Client


class VerifyLogicImpl(dao: LoginDao, bcrypt: AsyncBcrypt) extends VerifyLogic {

  override def verifyCredentials(credentials: UsernamePassword): Task[Client] =
    (for {
      pass   <- OptionT.fromOption[Task](credentials.password)
      client <- OptionT(dao.findClientByUsername(credentials.username))
      result <- OptionT.liftF(bcrypt.verify(pass, client.passwordHash))
      if result
    } yield client)
      .getOrElseF(Task.raiseError[Client](IncorrectCredentials()))

  override def verifyToken(client: Client): Task[VerifiedClient] =
    (for {
      id <- client.id
      token <- client.token
    } yield VerifiedClient(id, client.login, TinkoffToken(token)))
      .fold(Task.raiseError[VerifiedClient](TokenDoesNotExist()))(Task.now)

}
