package ru.zkerriga.investment.storage.queries

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import ru.zkerriga.investment.storage.DIO
import ru.zkerriga.investment.storage.entities.Client
import ru.zkerriga.investment.storage.tables.ClientsTable


object ClientsQueryRepository {
  val AllClients = TableQuery[ClientsTable]

  def addClient(login: String, passwordHash: String): DIO[Long, Effect.Write] =
    (AllClients returning AllClients.map(_.id)) += Client(None, login, passwordHash, None)

  private def queryById(id: Long): Query[ClientsTable, Client, Seq] =
    AllClients.filter(_.id === id)

  def findById(id: Long): DIO[Option[Client], Effect.Read] =
    queryById(id)
      .result
      .headOption

  def findByLogin(login: String): DIO[Option[Client], Effect.Read] =
    AllClients
      .filter(_.login === login)
      .result
      .headOption

  def inactiveClient(id: Long): DIO[Int, Effect.Write] =
    queryById(id)
      .map(_.active).update(false)

  def updateToken(id: Long, token: String): DIO[Int, Effect.Write] =
    queryById(id)
      .map(_.token).update(Some(token))
}
