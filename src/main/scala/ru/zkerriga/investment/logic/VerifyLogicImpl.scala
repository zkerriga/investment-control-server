package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.exceptions.{DatabaseError, IncorrectCredentials, TokenDoesNotExist}
import ru.zkerriga.investment.entities.{TinkoffToken, VerifiedClient}
import ru.zkerriga.investment.storage.LoginDao
import ru.zkerriga.investment.storage.entities.Client


class VerifyLogicImpl(dao: LoginDao, bcrypt: AsyncBcrypt) extends VerifyLogic {

  def findClientInDb(username: String): EitherT[Task, Either[DatabaseError, IncorrectCredentials], Client] =
    dao.findClientByUsername(username)
      .leftMap(Left.apply)
      .flatMap { optClient =>
        EitherT.fromOption(optClient, Right(IncorrectCredentials()))
      }

  def verityPassword(client: Client, password: String): EitherT[Task, IncorrectCredentials, Client] =
    EitherT(
      bcrypt.verify(password, client.passwordHash) map { check =>
        Either.cond(check, client, IncorrectCredentials())
      }
    )

  override def verifyCredentials(credentials: UsernamePassword): EitherT[Task, Either[DatabaseError, IncorrectCredentials], Client] =
    credentials.password.fold(
      EitherT.leftT[Task, Client](Right(IncorrectCredentials()).withLeft[DatabaseError])
    ) { password =>
      for {
        client    <- findClientInDb(credentials.username)
        verified  <- verityPassword(client, password).leftMap(err => Right(err).withLeft[DatabaseError])
      } yield verified
    }

  override def verifyToken(client: Client): EitherT[Task, TokenDoesNotExist, VerifiedClient] =
    EitherT.fromOption(
      for {
        id <- client.id
        token <- client.token
      } yield VerifiedClient(id, client.login, TinkoffToken(token)),
      TokenDoesNotExist()
    )

}
