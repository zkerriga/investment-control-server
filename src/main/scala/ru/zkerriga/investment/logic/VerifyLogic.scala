package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.VerifiedClient
import ru.zkerriga.investment.exceptions.{DatabaseError, IncorrectCredentials, TokenDoesNotExist}
import ru.zkerriga.investment.storage.entities.Client


trait VerifyLogic {

  def verifyCredentials(credentials: UsernamePassword): EitherT[Task, Either[DatabaseError, IncorrectCredentials], Client]

  def verifyToken(client: Client): EitherT[Task, TokenDoesNotExist, VerifiedClient]

}
