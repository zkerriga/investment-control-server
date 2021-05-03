package ru.zkerriga.investment.storage

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}


case class Notification(
  id: Option[Long],
  clientId: Long,
  message: String,
  sent: Boolean = false
)

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

object NotificationsQueryRepository {
  val AllNotifications = TableQuery[NotificationsTable]

  def addNotification(clientId: Long, message: String): DIO[Long, Effect.Write] =
    (AllNotifications returning AllNotifications.map(_.id)) +=
      Notification(None, clientId, message)

}