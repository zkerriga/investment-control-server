package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.{InvalidToken, ServerInternalError, UsernameAlreadyExist}
import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.storage.entities.Client


trait RegisterLogic {

  def registerClient(login: Login): EitherT[Task, UsernameAlreadyExist, Unit]

  def updateToken(client: Client, token: TinkoffToken): EitherT[Task, Either[ServerInternalError, InvalidToken], Unit]

}
