package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import monix.execution.Scheduler
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import ru.zkerriga.investment.entities.openapi.SandboxSetCurrencyBalanceRequest
import ru.zkerriga.investment.configuration.Configuration
import ru.zkerriga.investment.entities.TinkoffToken


class TinkoffOpenApiClientSpec extends TinkoffOpenApiClientTest {

  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = Scheduler(as.dispatcher)

  val api: OpenApiClient = new TinkoffOpenApiClient(
    "https://api-invest.tinkoff.ru/openapi/sandbox",
    SandboxSetCurrencyBalanceRequest("USD", 10000)
  )

  override protected def afterAll(): Unit =
    Await.result(
      as.terminate,
      Duration.Inf
    )

  override val validToken: TinkoffToken =
    TinkoffToken(ConfigSource.default.load[Configuration]
      .map(_.tinkoff.token).getOrElse(""))
}
