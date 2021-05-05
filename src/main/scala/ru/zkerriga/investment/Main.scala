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

  def createServiceApi(dao: Dao): ServiceLogic = {
    val tinkoffOpenApiClient: OpenApiClient = new TinkoffOpenApiClient
    val encryption: AsyncBcrypt = new AsyncBcryptImpl
    new ServiceLogicImpl(encryption, tinkoffOpenApiClient, dao)
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
    val dao: Dao                    = ServerDatabase
    val service: ServiceLogic       = createServiceApi(dao)
    val serverRoutes: ServerRoutes  = createServerRoutes(service)
    val server                      = Server(serverRoutes)

    val program = for {
      http <- server.start(baseUrl)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- Task.parZip2(terminateSystem, dao.close())
    } yield ()

    program.runSyncUnsafe()
  }

}
