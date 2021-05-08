package ru.zkerriga.investment.logic

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import scala.concurrent.duration._

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi.MarketOrderRequest
import ru.zkerriga.investment.storage.MonitoringDao
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class StocksMonitoring(openApiClient: OpenApiClient, dao: MonitoringDao, token: TinkoffToken) extends LazyLogging {

  private def getStockPrice(figi: String): Task[(String, Option[Double])] =
    openApiClient.`/market/orderbook`(token, figi) map { response =>
      figi -> response.payload.lastPrice
    }

  private def stockPricesToMap(stockPrices: Seq[(String, Option[Double])]): Map[String, Double] =
    stockPrices.collect {
      case (figi, Some(price)) => (figi, price)
    }.toMap

  private def filterStocksForSale(stocks: Seq[TrackStock], stockPrices: Map[String, Double]): Seq[TrackStock] =
    stocks.filter { stock =>
      stockPrices.get(stock.figi).fold(false) { price =>
        price >= stock.takeProfit || price <= stock.stopLoss
      }
    }

  private def saleStocks(stocks: Seq[TrackStock]): Task[Unit] =
    Task.parTraverseUnordered(stocks) { stock =>
      openApiClient.`/orders/market-order`(
        token,
        stock.figi,
        MarketOrderRequest(stock.lots, "Sell")
      )
    } *> dao.markStocksUntracked(
      stocks.collect { case stock if stock.id.isDefined => stock.id.getOrElse(0) }
    )

  private def createNotifications(stocks: Seq[TrackStock]): Task[Unit] = Task(
    stocks collect {
      case TrackStock(Some(id), clientId, _, _, _, _, _) =>
        Notification(None, clientId = clientId, trackStockId = id)
    }).flatMap { notifications => dao.addNotifications(notifications) }

  private def loop: Task[Unit] = for {
    trackedStocks <- dao.getAllTrackedStocks
    stockPricesSeq <- Task.parTraverseUnordered(trackedStocks.map(_.figi).distinct)(getStockPrice)
    stocksForSale = filterStocksForSale(trackedStocks, stockPricesToMap(stockPricesSeq))
    _ <- Task.parZip2(createNotifications(stocksForSale), saleStocks(stocksForSale))

    _ <- Task.now(logger.info({
      val sold = stocksForSale.size
      s"""Monitoring Cycle =>
         |Supported now: ${trackedStocks.size - sold}
         |Sold now: $sold""".stripMargin
    }))
    result <- Task.defer(loop.delayExecution(5.second))
  } yield result

  def start: Task[Unit] = loop
}
