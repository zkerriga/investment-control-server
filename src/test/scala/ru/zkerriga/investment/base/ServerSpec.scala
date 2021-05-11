package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.execution.Scheduler
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import ru.zkerriga.investment.storage.queries._
import ru.zkerriga.investment.configuration.{Configuration, DatabaseConf, Port, ServerConf}
import ru.zkerriga.investment.logic.TinkoffOpenApiClient
import ru.zkerriga.investment.{Main, Server}
import ru.zkerriga.investment.entities.TinkoffToken


class ServerSpec extends ServerISpecBase {

  implicit lazy val as: ActorSystem = ActorSystem()
  implicit lazy val s: Scheduler = Scheduler(as.dispatcher)

  override protected def beforeAll(): Unit = {
    Await.result(Future(db.run(initSchema)) flatMap (_ => server), Duration.Inf)
  }

  override protected def afterAll(): Unit = {
    Await.result(
      server
        .flatMap(_.terminate(10.seconds))
        .flatMap(_ => as.terminate())
        .flatMap(_ => Future(db.close())),
      Duration.Inf
    )
  }

  private val databaseConf = DatabaseConf(
    url = s"jdbc:h2:mem:${java.util.UUID.randomUUID()}",
    user = "",
    password = "",
    driver = "org.h2.Driver",
    maxThreadPool = None
  )
  private val serverConf = ServerConf(host = "localhost", port = Port(8080), useHttps = false)

  private val db = Database.forURL(
    url = databaseConf.url,
    user = databaseConf.user,
    password = databaseConf.password,
    driver = databaseConf.driver,
    keepAliveConnection = true
  )

  private val initSchema =
    (ClientsQueryRepository.AllClients.schema ++
      TrackStocksQueryRepository.AllTrackStocks.schema ++
      NotificationsQueryRepository.AllNotifications.schema).create

  private val configuration = Main.getConfiguration

  private def server: Future[Http.ServerBinding] = {
    configuration.runToFuture flatMap { config =>
      Server(
        Main.createServerRoutes(
          Main.createQueryRunner(db),
          new TinkoffOpenApiClient(config.tinkoff.url, config.tinkoff.startBalance), serverConf)
      ).start(serverConf).runToFuture
    }
  }

  override val validToken: TinkoffToken =
    TinkoffToken(ConfigSource.default.load[Configuration]
      .map(_.tinkoff.token).getOrElse(""))

}
