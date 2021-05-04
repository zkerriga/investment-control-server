package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.storage.{ClientsQueryRepository, TrackStocksQueryRepository}
import ru.zkerriga.investment.storage.entities.{Client, Notification, TrackStock}


class NotificationsTable(tag: Tag) extends Table[Notification](tag, "NOTIFICATIONS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("CLIENT_ID")
  def trackStockId: Rep[Long] = column("TRACK_STOCK_ID")
  def sold: Rep[Boolean] = column("SOLD")
  def sent: Rep[Boolean] = column("SENT")

  /* todo: change it with flyway */
  def client: ForeignKeyQuery[ClientsTable, Client] =
    foreignKey("CLIENT_FOR_NOTIFICATION_FK", clientId, ClientsQueryRepository.AllClients)(_.id)
  def trackStock: ForeignKeyQuery[TrackStocksTable, TrackStock] =
    foreignKey("TRACK_STOCK_FK", trackStockId, TrackStocksQueryRepository.AllTrackStocks)(_.id)

  override def * : ProvenShape[Notification] = (id.?, clientId, trackStockId, sold, sent).mapTo[Notification]
}
