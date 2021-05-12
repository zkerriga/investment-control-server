package ru.zkerriga.investment.monitoring

import monix.eval.Task

import ru.zkerriga.investment.exceptions.ServerInternalError


trait StockMonitoring {
  def start: Task[Either[ServerInternalError, Unit]]
}
