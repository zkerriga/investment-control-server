package ru.zkerriga.investment.storage.tables

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.storage.queries.ClientsQueryRepository
import ru.zkerriga.investment.storage.entities.{Client, TrackStock}


class TrackStocksTable(tag: Tag) extends Table[TrackStock](tag, "TRACK_STOCKS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("CLIENT_ID")
  def figi: Rep[String] = column("FIGI")
  def lots: Rep[Int] = column("LOTS")
  def stopLoss: Rep[Double] = column("STOP_LOSS")
  def takeProfit: Rep[Double] = column("TAKE_PROFIT")
  def active: Rep[Boolean] = column("ACTIVE")

  override def * : ProvenShape[TrackStock] = (id.?, clientId, figi, lots, stopLoss, takeProfit, active).mapTo[TrackStock]
}
