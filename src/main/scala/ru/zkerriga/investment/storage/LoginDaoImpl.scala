package ru.zkerriga.investment.storage

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.storage.queries.ClientsQueryRepository


class LoginDaoImpl(queryRunner: QueryRunner[Task]) extends LoginDao {

  override def registerClient(username: String, hash: String): EitherT[Task, DatabaseError, Long] =
    queryRunner.run(ClientsQueryRepository.addClient(username, hash))

  override def findClientByUsername(username: String): EitherT[Task, DatabaseError, Option[Client]] =
    queryRunner.run(ClientsQueryRepository.findByLogin(username))

  override def updateClientToken(clientId: Long, token: String): EitherT[Task, DatabaseError, Unit] =
    queryRunner.run(ClientsQueryRepository.updateToken(clientId, token)) map (_ => ())

}
