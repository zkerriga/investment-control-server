package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

import ru.zkerriga.investment.storage.entities.Client


class ClientsTable(tag: Tag) extends Table[Client](tag, "clients") {
  def id: Rep[Long] = column("id", O.PrimaryKey, O.AutoInc)
  def login: Rep[String] = column("login", O.Unique)
  def passwordHash: Rep[String] = column("password_hash")
  def token: Rep[Option[String]] = column("token")
  def active: Rep[Boolean] = column("active")

  override def * : ProvenShape[Client] = (id.?, login, passwordHash, token, active).mapTo[Client]
}
