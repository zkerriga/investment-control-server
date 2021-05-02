package ru.zkerriga.investment

import akka.actor.ActorSystem
import monix.eval.Task
import monix.execution.Scheduler

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.logic._
import ru.zkerriga.investment.api._


object Main {
  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = monix.execution.Scheduler.global

  private def terminateSystem = Task.fromFuture(as.terminate())

  def createServiceApi: ServiceLogic = {
    val tinkoffOpenApiClient: OpenApiClient = new TinkoffOpenApiClient
    val encryption: AsyncBcrypt = new AsyncBcryptImpl
    new ServiceLogicImpl(encryption, tinkoffOpenApiClient)
  }

  def createServerRoutes(service: ServiceLogic): ServerRoutes = {
    val exceptionHandler: ExceptionHandler[Task] = ExceptionHandlerForTask()
    new ServerRoutesImpl(
      List(
        new MarketServerEndpoint(service, exceptionHandler),
        new RegisterServerEndpoint(service, exceptionHandler)
      )
    )
  }

  def main(args: Array[String]): Unit = {
    val service: ServiceLogic       = createServiceApi
    val serverRoutes: ServerRoutes  = createServerRoutes(service)
    val server                      = Server(serverRoutes)

    val program = for {
      http <- server.start("localhost", 8080)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- terminateSystem
    } yield ()

    program.runSyncUnsafe()
  }

}
