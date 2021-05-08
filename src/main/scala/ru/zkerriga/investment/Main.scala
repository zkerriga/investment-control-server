package ru.zkerriga.investment

import akka.actor.{ActorSystem, Terminated}
import akka.http.scaladsl.model.Uri
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.logic._
import ru.zkerriga.investment.api._
import ru.zkerriga.investment.api.endpoints._
import ru.zkerriga.investment.storage._


object Main {
  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = monix.execution.Scheduler.global

  val baseUrl: Uri = Uri(
    scheme = "http",
    authority = Uri.Authority(host = Uri.Host("localhost"), port = 8080)
  )

  private def terminateSystem: Task[Terminated] = Task.fromFuture(as.terminate())

  def createServiceApi(dao: ClientsDao, openApiClient: OpenApiClient): ServiceLogic = {
    val encryption: AsyncBcrypt = new AsyncBcryptImpl
    new ServiceLogicImpl(encryption, openApiClient, dao)
  }

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

  def main(args: Array[String]): Unit = {
    val dao: ServerDatabase.type      = ServerDatabase
    val openApiClient: OpenApiClient  = new TinkoffOpenApiClient
    val service: ServiceLogic         = createServiceApi(dao, openApiClient)
    val serverRoutes: ServerRoutes    = createServerRoutes(service)
    val server                        = Server(serverRoutes)
    val monitor                       = new StocksMonitoring(openApiClient, dao)

    val program = for {
      http <- server.start(baseUrl)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- Task.parZip2(terminateSystem, dao.close())
    } yield ()

    Task.racePair(program, monitor.start).foreach {
      case Left((_, fiber)) => fiber.cancel
      case Right((fiber, _)) => fiber.cancel
    }
  }

}
