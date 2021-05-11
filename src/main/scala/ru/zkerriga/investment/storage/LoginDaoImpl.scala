package ru.zkerriga.investment.storage

import monix.eval.Task

import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.storage.queries.ClientsQueryRepository


class LoginDaoImpl(queryRunner: QueryRunner[Task]) extends LoginDao {

  override def registerClient(username: String, hash: String): Task[Long] =
    queryRunner.run(ClientsQueryRepository.addClient(username, hash))

  override def findClientByUsername(username: String): Task[Option[Client]] =
    queryRunner.run(ClientsQueryRepository.findByLogin(username))

  override def updateClientToken(clientId: Long, token: String): Task[Unit] =
    queryRunner.run(ClientsQueryRepository.updateToken(clientId, token)) flatMap (_ => Task.unit)

}
