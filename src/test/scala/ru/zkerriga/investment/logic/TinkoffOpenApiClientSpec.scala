package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import monix.execution.Scheduler
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import ru.zkerriga.investment.TokenForTest
import ru.zkerriga.investment.entities.TinkoffToken


class TinkoffOpenApiClientSpec extends TinkoffOpenApiClientTest {
  val validToken: TinkoffToken = TokenForTest.token

  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = monix.execution.Scheduler.global

  val api: OpenApiClient = new TinkoffOpenApiClient()

  override protected def afterAll(): Unit =
    Await.result(
      as.terminate,
      Duration.Inf
    )

}
