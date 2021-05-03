package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.execution.Scheduler
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import ru.zkerriga.investment.storage.ServerDatabase
import ru.zkerriga.investment.logic.{AsyncBcryptImpl, ServiceLogicImpl, TinkoffOpenApiClient}
import ru.zkerriga.investment.{Main, Server}


class ServerSpec extends ServerISpecBase {

  implicit lazy val as: ActorSystem = ActorSystem()
  implicit lazy val s: Scheduler = monix.execution.Scheduler.global

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

  private val server: Future[Http.ServerBinding] =
    Server(
      Main.createServerRoutes(
        new ServiceLogicImpl(new AsyncBcryptImpl, new TinkoffOpenApiClient, ServerDatabase)
      )
    ).start(interface, port).runToFuture

}
