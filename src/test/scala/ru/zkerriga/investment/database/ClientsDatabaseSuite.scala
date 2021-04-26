package ru.zkerriga.investment.database

import org.scalactic.source
import org.scalatest.compatible
import org.scalatest.funsuite.AsyncFunSuite

import ClientsQueryRepository.AllClients

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.H2Profile.api._


abstract class ClientsDatabaseSuite extends AsyncFunSuite {
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
          .andThen(AllClients ++= sampleClients)
      ).flatMap(_ => db.run(testFun)).andThen { case _ => db.close() }
    }
  }

  private val initSchema =
    (ClientsQueryRepository.AllClients.schema).create /* todo: use flyway to create db */

  protected val sampleClients = Seq(
    Client(1, "login1", "hash:something1", None),
    Client(2, "login2", "hash:something2", Some("token:something2")),
  )
}
