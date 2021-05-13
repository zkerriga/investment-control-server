package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.{InvalidToken, ProgrammedError, ServerInternalError, UsernameAlreadyExist}
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.storage.LoginDao
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.utils.AsyncBcrypt


class RegisterLogicImpl(dao: LoginDao, bcrypt: AsyncBcrypt, openApiClient: OpenApiClient) extends RegisterLogic {

  override def registerClient(login: Login): EitherT[Task, UsernameAlreadyExist, Unit] =
    EitherT.right(bcrypt.hash(login.password)) flatMap { hash =>
      dao.registerClient(login.username, hash)
        .leftMap(_ => UsernameAlreadyExist(login.username))
        .map(_ => ())
    }

  override def updateToken(client: Client, token: TinkoffToken): EitherT[Task, Either[ServerInternalError, InvalidToken], Unit] =
    client.id.fold(
      EitherT.leftT[Task, Unit](
        Left(ProgrammedError("Client without id"): ServerInternalError).withRight[InvalidToken]
      )
    ) { id =>
      openApiClient.`/sandbox/register`(token)
        .leftMap(_ => Right(InvalidToken()))
        .flatMap { _ =>
          dao.updateClientToken(id, token.token)
            .leftMap(Left.apply)
        }
    }

}
