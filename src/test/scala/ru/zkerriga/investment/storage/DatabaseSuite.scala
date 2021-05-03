package ru.zkerriga.investment.storage

import org.scalactic.source
import org.scalatest.compatible
import org.scalatest.funsuite.AsyncFunSuite

import ClientsQueryRepository.AllClients
import TrackStocksQueryRepository.AllTrackStocks
import NotificationsQueryRepository.AllNotifications

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.H2Profile.api._

import ru.zkerriga.investment.storage.entities._


abstract class DatabaseSuite extends AsyncFunSuite {
  protected def test[R, S <: NoStream, E <: Effect](testName: String)
                                                   (testFun: DBIOAction[compatible.Assertion, S, E])
                                                   (implicit pos: source.Position): Unit = {
    super.test(testName) {
      val db = Database.forURL(
        s"jdbc:h2:mem:${java.util.UUID.randomUUID()}",
        driver = "org.h2.Driver",
        keepAliveConnection = true
      )

      db.run(
        initSchema
          .andThen(AllClients.forceInsertAll(sampleClients))
          .andThen(AllTrackStocks.forceInsertAll(sampleTrackStocks))
          .andThen(AllNotifications.forceInsertAll(sampleNotifications))
      ).flatMap(_ => db.run(testFun)).andThen { case _ => db.close() }
    }
  }

  private val initSchema =
    (ClientsQueryRepository.AllClients.schema ++
      TrackStocksQueryRepository.AllTrackStocks.schema ++
      NotificationsQueryRepository.AllNotifications.schema).create /* todo: use flyway to create db */

  protected val sampleClients = Seq(
    Client(Some(1), "login1", "hash:something1", None),
    Client(Some(2), "login2", "hash:something2", Some("token:something2")),
  )

  protected val sampleTrackStocks = Seq(
    TrackStock(Some(1), clientId = 1, "FIGI1", 10.0, 40.0),
    TrackStock(Some(2), clientId = 1, "FIGI2", 100.0, 110.0),
    TrackStock(Some(3), clientId = 2, "FIGI3", 10.0, 40.0)
  )

  protected val sampleNotifications = Seq(
    Notification(Some(1), clientId = 2, "First hello!"),
    Notification(Some(2), clientId = 2, "Second hello!")
  )
}
