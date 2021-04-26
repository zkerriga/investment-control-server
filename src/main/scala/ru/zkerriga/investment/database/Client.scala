package ru.zkerriga.investment.database

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

import java.time.Instant.now

case class Client(
  id: Long,
  login: String,
  passwordHash: String,
  token: Option[String],
  active: Boolean = true
)

class ClientsTable(tag: Tag) extends Table[Client](tag, "CLIENTS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey)
  def login: Rep[String] = column("LOGIN", O.Unique)
  def passwordHash: Rep[String] = column("PASSWORD_HASH")
  def token: Rep[Option[String]] = column("TOKEN")
  def active: Rep[Boolean] = column("ACTIVE")

  override def * : ProvenShape[Client] = (id, login, passwordHash, token, active).mapTo[Client]
}

object ClientsQueryRepository {
  val AllClients = TableQuery[ClientsTable]

  def addClient(login: String, passwordHash: String): DIO[Int, Effect.Write] =
    AllClients += Client(now.toEpochMilli, login, passwordHash, None)

  private def queryByLogin(login: String): Query[ClientsTable, Client, Seq] =
    AllClients.filter(_.login === login)

  def findByLogin(login: String): DIO[Option[Client], Effect.Read] =
    queryByLogin(login)
      .result
      .headOption

  def inactiveClient(login: String): DIO[Int, Effect.Write] =
    queryByLogin(login)
      .map(_.active).update(false)

  def updateToken(login: String, token: String): DIO[Int, Effect.Write] =
    queryByLogin(login)
      .map(_.token).update(Some(token))
}