package ru.zkerriga.investment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.execution.Scheduler
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import ru.zkerriga.investment.base.ServerISpecBase

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

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

  private lazy val server: Future[Http.ServerBinding] =
    Server().start(interface, port).runToFuture

}
