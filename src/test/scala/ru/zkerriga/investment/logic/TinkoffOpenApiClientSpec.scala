package ru.zkerriga.investment.logic

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import monix.execution.Scheduler
import pureconfig.ConfigSource
import ru.zkerriga.investment.configuration.{Configuration, TinkoffConf}
import ru.zkerriga.investment.entities.TinkoffToken

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import ru.zkerriga.investment.entities.openapi.SandboxSetCurrencyBalanceRequest


class TinkoffOpenApiClientSpec extends TinkoffOpenApiClientTest {

  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = Scheduler(as.dispatcher)

  val api: OpenApiClient = new TinkoffOpenApiClient(
    Uri("https://api-invest.tinkoff.ru/openapi/sandbox"),
    SandboxSetCurrencyBalanceRequest("USD", 1000)
  )

  override protected def afterAll(): Unit =
    Await.result(
      as.terminate,
      Duration.Inf
    )

  import pureconfig.generic.auto._
  override def validToken: TinkoffToken =
    TinkoffToken(ConfigSource.default.load[TinkoffConf].map(_.token).getOrElse(""))
}
