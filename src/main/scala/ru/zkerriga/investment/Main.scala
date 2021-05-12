package ru.zkerriga.investment

import akka.actor.ActorSystem
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.logic._
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._
import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.storage._
import ru.zkerriga.investment.configuration._
import ru.zkerriga.investment.monitoring.StocksMonitoring


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
    Task(new TinkoffOpenApiClient(config.url, config.startBalance))

  def main(args: Array[String]): Unit = {

    def logic(config: Configuration, runner: QueryRunner[Task]): Task[Unit] = for {
      api <- createOpenApiClient(config.tinkoff)
      _   <- Task.race(
        initServer(runner, api, config.server),
        initMonitor(runner, api, TinkoffToken(config.tinkoff.token))
      )
    } yield ()

    val program: Task[Unit] = for {
      config  <- Configuration.getConfiguration
      _       <- Migration.migrate(config.database)
      db      <- DatabaseHelper.createDb(config.database)
      runner   = DatabaseHelper.createQueryRunner(db)
      _       <- logic(config, runner).doOnFinish(_ => Task(db.close()))
    } yield ()

    program
      .onErrorRecoverWith(error => Console.putAnyLn(error.getMessage))
      .doOnFinish(_ => terminateSystem)
      .runSyncUnsafe()
  }

}
