package ru.zkerriga.investment.logic

import monix.eval.Task
import scala.concurrent.duration._

import ru.zkerriga.investment.logging.Console
import ru.zkerriga.investment.storage.MonitoringDao


class StocksMonitoring(openApiClient: OpenApiClient, dao: MonitoringDao) {

  private def loop: Task[Unit] = for {
    _ <- Console.putAnyLn("Check")
    result <- Task.defer(loop.delayExecution(1.second))
  } yield result

  def start: Task[Unit] = loop.executeAsync
}
