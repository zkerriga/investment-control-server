package ru.zkerriga.investment.logic

import cats.implicits._
import cats.data.EitherT
import monix.eval.Task
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap

import ru.zkerriga.investment.exceptions.DatabaseError
import ru.zkerriga.investment.storage.LoginDao
import ru.zkerriga.investment.storage.entities.Client


class FakeLoginDao extends LoginDao {

  val clientsId: AtomicInteger = new AtomicInteger(0)
  val clientsTable: TrieMap[Long, Client] = new TrieMap[Long, Client]

  private def findClientByUsernameInTable(username: String): Option[(Long, Client)] =
    clientsTable.readOnlySnapshot.find {
      case (_, client) => client.login === username
    }

  override def registerClient(username: String, hash: String): EitherT[Task, DatabaseError, Long] = {
    EitherT.fromEither(
      findClientByUsernameInTable(username) match {
        case Some(_) => Left(DatabaseError())
        case None =>
          val id: Long = clientsId.getAndIncrement
          clientsTable.addOne((id, Client(Some(id), username, hash, None)))
          Right(id)
      }
    )
  }

  override def findClientByUsername(username: String): EitherT[Task, DatabaseError, Option[Client]] =
    EitherT.rightT(findClientByUsernameInTable(username).map(_._2))

  override def updateClientToken(clientId: Long, token: String): EitherT[Task, DatabaseError, Unit] = {
    EitherT.fromOption(
      clientsTable.readOnlySnapshot.get(clientId).map { client =>
        clientsTable.update(clientId, client.copy(token = Some(token)))
      },
      DatabaseError()
    )
  }

}
