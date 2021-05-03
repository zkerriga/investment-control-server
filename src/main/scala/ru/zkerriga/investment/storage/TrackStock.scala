package ru.zkerriga.investment.storage

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import ru.zkerriga.investment.entities.StockOrder


case class TrackStock(
  id: Option[Long],
  clientId: Long,
  figi: String,
  stopLoss: Double,
  takeProfit: Double,
  active: Boolean = true
)

class TrackStocksTable(tag: Tag) extends Table[TrackStock](tag, "TRACK_STOCKS") {
  def id: Rep[Long] = column("ID", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Long] = column("CLIENT_ID")
  def figi: Rep[String] = column("FIGI")
  def stopLoss: Rep[Double] = column("STOP_LOSS")
  def takeProfit: Rep[Double] = column("TAKE_PROFIT")
  def active: Rep[Boolean] = column("ACTIVE")

  /* todo: change it with flyway */
  def client: ForeignKeyQuery[ClientsTable, Client] =
    foreignKey("CLIENT_FK", clientId, ClientsQueryRepository.AllClients)(_.id)

  override def * : ProvenShape[TrackStock] = (id.?, clientId, figi, stopLoss, takeProfit, active).mapTo[TrackStock]
}

object TrackStocksQueryRepository {
  val AllTrackStocks = TableQuery[TrackStocksTable]

  def addTrackStock(clientId: Long, stockOrder: StockOrder): DIO[Long, Effect.Write] =
    (AllTrackStocks returning AllTrackStocks.map(_.id)) +=
      TrackStock(None, clientId, stockOrder.figi, stockOrder.stopLoss, stockOrder.takeProfit)

}