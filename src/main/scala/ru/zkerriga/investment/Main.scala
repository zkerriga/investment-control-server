package ru.zkerriga.investment

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import monix.eval.Task
import monix.execution.Scheduler
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.logic._
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.storage._
import ru.zkerriga.investment.configuration.{Configuration, DatabaseConf, ServerConf, TinkoffConf}


object Main {
  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = Scheduler(as.dispatcher)

  private def terminateSystem: Task[Unit] =
    Task.fromFuture(as.terminate()) *> Task.unit

  def createServerRoutes(runner: QueryRunner[Task], apiClient: OpenApiClient, config: ServerConf): ServerRoutes = {
    val encryption: AsyncBcrypt               = new AsyncBcryptImpl
    val exceptionHandler                      = ExceptionHandlerForTask()

    val loginDao: LoginDao                    = new LoginDaoImpl(runner)
    val clientDao: ClientDao                  = new ClientDaoImpl(runner)

    val registerLogic: RegisterLogic          = new RegisterLogicImpl(loginDao, encryption, apiClient)
    val marketLogic: MarketLogic              = new MarketLogicImpl(clientDao, apiClient)
    val verifyLogic: VerifyLogic              = new VerifyLogicImpl(loginDao, encryption)
    val notificationLogic: NotificationLogic  = new NotificationLogicImpl(clientDao)

    new ServerRoutesImpl(
      List(
        new RegisterServerEndpoint(registerLogic, verifyLogic, exceptionHandler),
        new MarketServerEndpoint(verifyLogic, marketLogic, exceptionHandler),
        new OrdersServerEndpoint(verifyLogic, marketLogic, exceptionHandler),
        new NotificationsServerEndpoint(verifyLogic, notificationLogic, exceptionHandler),
      ),
      config
    )
  }

  def initServer(queryRunner: QueryRunner[Task], openApiClient: OpenApiClient, config: ServerConf): Task[Unit] = {
    val serverRoutes: ServerRoutes = createServerRoutes(queryRunner, openApiClient, config)
    val server = Server(serverRoutes)

    for {
      http <- server.start(config)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
    } yield ()
  }

  def initMonitor(queryRunner: QueryRunner[Task], openApiClient: OpenApiClient, token: TinkoffToken): Task[Unit] = {
    val dao     = new MonitoringDaoImpl(queryRunner)
    val monitor = new StocksMonitoring(openApiClient, dao, token)
    monitor.start
  }

  def createOpenApiClient(config: TinkoffConf): Task[OpenApiClient] =
    Task(new TinkoffOpenApiClient(Uri(config.url), config.startBalance))

  def getConfiguration: Task[Configuration] = {
    import pureconfig.generic.auto._
    val configSource = ConfigSource.default.load[Configuration]

    Task.fromEither(
      (err: ConfigReaderFailures) => new RuntimeException(s"Invalid configuration: $err")
    )(configSource)
  }

  def createDb(config: DatabaseConf): Task[Database] = Task {
    Database.forURL(
      url = config.url,
      user = config.user,
      password = config.password,
      driver = config.driver
    )
  }

  def createQueryRunner(db: Database): QueryRunner[Task] = new QueryRunner[Task](db) {
    override protected def converter[A]: Future[A] => Task[A] =
      future => Task.deferFutureAction { implicit scheduler => future }
  }

  def main(args: Array[String]): Unit = {

    def logic(config: Configuration, runner: QueryRunner[Task]): Task[Unit] = for {
      api <- createOpenApiClient(config.tinkoff)
      _   <- Task.race(
        initServer(runner, api, config.server),
        initMonitor(runner, api, TinkoffToken(config.tinkoff.token))
      )
    } yield ()

    val program: Task[Unit] = for {
      config  <- getConfiguration
      _       <- Migration.migrate(config.database)
      db      <- createDb(config.database)
      _       <- logic(config, createQueryRunner(db)).doOnFinish(_ => Task(db.close()))
    } yield ()

    program
      .onErrorRecoverWith(error => Console.putAnyLn(error.getMessage))
      .doOnFinish(_ => terminateSystem)
      .runSyncUnsafe()
  }

}
