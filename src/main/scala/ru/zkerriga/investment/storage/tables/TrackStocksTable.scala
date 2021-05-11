package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.storage.queries.ClientsQueryRepository
import ru.zkerriga.investment.storage.entities.{Client, TrackStock}


class TrackStocksTable(tag: Tag) extends Table[TrackStock](tag, "track_stocks") {
  def id: Rep[Long] = column("id", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("client_id")
  def figi: Rep[String] = column("figi")
  def lots: Rep[Int] = column("lots")
  def stopLoss: Rep[Double] = column("stop_loss")
  def takeProfit: Rep[Double] = column("take_profit")
  def active: Rep[Boolean] = column("active")

  override def * : ProvenShape[TrackStock] = (id.?, clientId, figi, lots, stopLoss, takeProfit, active).mapTo[TrackStock]
}
