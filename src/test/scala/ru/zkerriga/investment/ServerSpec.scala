package ru.zkerriga.investment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.execution.Scheduler
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import ru.zkerriga.investment.api.ServiceLogicImpl
import ru.zkerriga.investment.base.ServerISpecBase
import ru.zkerriga.investment.logic.{AsyncBcrypt, AsyncBcryptImpl, OpenApiClient, ServiceLogic, ServiceLogicImpl, TinkoffOpenApiClient}
import ru.zkerriga.investment.routes.TapirRoutes


class ServerSpec extends ServerISpecBase {

  implicit lazy val as: ActorSystem = ActorSystem()
  implicit lazy val s: Scheduler = monix.execution.Scheduler.global

  val tinkoffOpenApiClient: OpenApiClient = new TinkoffOpenApiClient
  val encryption: AsyncBcrypt = new AsyncBcryptImpl
  val service: ServiceLogic = new ServiceLogicImpl(encryption, tinkoffOpenApiClient)
  val endpoints = new TapirRoutes(service)

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

  private lazy val server: Future[Http.ServerBinding] =
    Server(Main.createServerRoutes(Main.createServiceApi))
      .start(interface, port).runToFuture

}
