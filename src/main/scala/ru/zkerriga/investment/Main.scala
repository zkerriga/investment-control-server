package ru.zkerriga.investment

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import monix.eval.Task
import monix.execution.Scheduler
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.logic._
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.storage._
import ru.zkerriga.investment.configuration.Configuration

object Main {
  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = Scheduler(as.dispatcher)

  lazy val baseUrl: Uri = Uri(
    scheme = "http",
    authority = Uri.Authority(host = Uri.Host("localhost"), port = 8080)
  )

  private def terminateSystem: Task[Unit] =
    Task.fromFuture(as.terminate()) *> Task.unit

  def createServerRoutes(service: ServiceLogic): ServerRoutes = {
    val exceptionHandler: ExceptionHandler[Task] = ExceptionHandlerForTask()
    new ServerRoutesImpl(
      List(
        new RegisterServerEndpoint(service, exceptionHandler),
        new MarketServerEndpoint(service, exceptionHandler),
        new OrdersServerEndpoint(service, exceptionHandler),
        new NotificationsServerEndpoint(service, exceptionHandler),
      ),
      baseUrl
    )
  }

  def initServer(dao: ClientsDao, openApiClient: OpenApiClient): Task[Unit] = {
    val encryption: AsyncBcrypt     = new AsyncBcryptImpl
    val serviceLogic: ServiceLogic  = new ServiceLogicImpl(encryption, openApiClient, dao)
    val serverRoutes: ServerRoutes  = createServerRoutes(serviceLogic)
    val server                      = Server(serverRoutes)

    for {
      http <- server.start(baseUrl)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
    } yield ()
  }

  def initMonitor(dao: MonitoringDao, openApiClient: OpenApiClient, token: TinkoffToken): Task[Unit] = {
    val monitor = new StocksMonitoring(openApiClient, dao, token)
    monitor.start
  }

  def createOpenApiClient: Task[OpenApiClient] =
    Task(new TinkoffOpenApiClient)

  def getConfiguration: Task[Configuration] = {
    import pureconfig.generic.auto._
    val configSource = ConfigSource.default.load[Configuration]

    Task.fromEither(
      (err: ConfigReaderFailures) => new RuntimeException(s"Invalid configuration: $err")
    )(configSource)
  }

  def main(args: Array[String]): Unit = {
    val program: Task[Unit] = for {
      config <- getConfiguration
      dao   <- Task(ServerDatabase)
      api   <- createOpenApiClient
      _     <- Task.race(initServer(dao, api), initMonitor(dao, api, TinkoffToken(config.tinkoff.token)))
      _     <- dao.close()
    } yield ()

    program
      .onErrorRecoverWith(error => Console.putAnyLn(error.getMessage))
      .doOnFinish(_ => terminateSystem)
      .runSyncUnsafe()
  }

}
