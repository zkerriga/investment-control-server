package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.storage.ClientsQueryRepository
import ru.zkerriga.investment.storage.entities.{Client, Notification}


class NotificationsTable(tag: Tag) extends Table[Notification](tag, "NOTIFICATIONS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("CLIENT_ID")
  def message: Rep[String] = column("MESSAGE")
  def sent: Rep[Boolean] = column("SENT")

  /* todo: change it with flyway */
  def client: ForeignKeyQuery[ClientsTable, Client] =
    foreignKey("CLIENT_FOR_NOTIFICATION_FK", clientId, ClientsQueryRepository.AllClients)(_.id)

  override def * : ProvenShape[Notification] = (id.?, clientId, message, sent).mapTo[Notification]
}
