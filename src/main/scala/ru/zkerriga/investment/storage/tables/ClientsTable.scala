package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

import ru.zkerriga.investment.storage.entities.Client


class ClientsTable(tag: Tag) extends Table[Client](tag, "CLIENTS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey, O.AutoInc)
  def login: Rep[String] = column("LOGIN", O.Unique)
  def passwordHash: Rep[String] = column("PASSWORD_HASH")
  def token: Rep[Option[String]] = column("TOKEN")
  def active: Rep[Boolean] = column("ACTIVE")

  override def * : ProvenShape[Client] = (id.?, login, passwordHash, token, active).mapTo[Client]
}
