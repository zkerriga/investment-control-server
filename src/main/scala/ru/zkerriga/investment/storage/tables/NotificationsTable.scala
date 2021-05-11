package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.storage.queries.{ClientsQueryRepository, TrackStocksQueryRepository}
import ru.zkerriga.investment.storage.entities.{Client, Notification, TrackStock}


class NotificationsTable(tag: Tag) extends Table[Notification](tag, "notifications") {
  def id: Rep[Long] = column("id", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("client_id")
  def trackStockId: Rep[Long] = column("track_stock_id")
  def sold: Rep[Boolean] = column("sold")
  def sent: Rep[Boolean] = column("sent")

  override def * : ProvenShape[Notification] = (id.?, clientId, trackStockId, sold, sent).mapTo[Notification]
}
