package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.execution.Scheduler
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import pureconfig.ConfigSource
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import ru.zkerriga.investment.storage.Migration
import ru.zkerriga.investment.configuration.{DatabaseConf, Port, ServerConf, TinkoffConf}
import ru.zkerriga.investment.logic.TinkoffOpenApiClient
import ru.zkerriga.investment.{Main, Server}
import ru.zkerriga.investment.entities.TinkoffToken


class ServerSpec extends ServerISpecBase {

  implicit lazy val as: ActorSystem = ActorSystem()
  implicit lazy val s: Scheduler = Scheduler(as.dispatcher)

  override protected def beforeAll(): Unit = {
    Await.result(server, Duration.Inf)
    ()
  }

  override protected def afterAll(): Unit = {
    Await.result(
      server.flatMap(_.terminate(10.seconds))
        .flatMap(_ => as.terminate()),
      Duration.Inf
    )
    ()
  }

  private val configuration = Main.getConfiguration.memoize

  private val server: Future[Http.ServerBinding] = {
    val databaseConf = DatabaseConf(
      url = s"jdbc:h2:mem:${java.util.UUID.randomUUID()}",
      user = "",
      password = "",
      driver = "org.h2.Driver",
      maxThreadPool = None
    )
    val serverConf = ServerConf(host = "0.0.0.0", port = Port(8080), useHttps = false)

    val db = Database.forURL(
      url = databaseConf.url,
      driver = databaseConf.driver,
      keepAliveConnection = true
    )

    (configuration <* Migration.migrate(databaseConf)).runToFuture flatMap { config =>
      Server(
        Main.createServerRoutes(
          Main.createQueryRunner(db),
          new TinkoffOpenApiClient(config.tinkoff.url, config.tinkoff.startBalance), serverConf)
      ).start(serverConf).runToFuture
    }
  }

  import pureconfig.generic.auto._
  override def validToken: TinkoffToken =
    TinkoffToken(ConfigSource.default.load[TinkoffConf].map(_.token).getOrElse(""))
}
