package ru.zkerriga.investment

import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import scala.concurrent.duration._

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, PlacedMarketOrder, TinkoffResponse}
import ru.zkerriga.investment.logic.OpenApiClient
import ru.zkerriga.investment.storage.MonitoringDao
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}


class StocksMonitoring(openApiClient: OpenApiClient, dao: MonitoringDao, token: TinkoffToken) extends LazyLogging {

  private def getStockPrice(figi: String): EitherT[Task, ResponseError, (String, Option[Double])] =
    openApiClient.`/market/orderbook`(token, figi) map {
      case TinkoffResponse(_, _, orderBook) => (figi, orderBook.lastPrice)
    }

  private def filterStocksForSale(stocks: Seq[TrackStock], stockPrices: Map[String, Double]): Seq[TrackStock] =
    stocks.filter { stock =>
      stockPrices.get(stock.figi).fold(false) { price =>
        price >= stock.takeProfit || price <= stock.stopLoss
      }
    }

  private def sendRequestToSaleStocks(stocks: Seq[TrackStock]): Task[Seq[Either[ResponseError, (Option[Long], TinkoffResponse[PlacedMarketOrder])]]] =
    Task.parTraverseUnordered(stocks) { stock =>
      openApiClient.`/orders/market-order`(
        token,
        stock.figi,
        MarketOrderRequest(stock.lots, "Sell")
      ).map(stock.id -> _).value
    }

  private def markSoldStocksUntrackedInDb(idWithResponses: Seq[Either[ResponseError, (Option[Long], TinkoffResponse[PlacedMarketOrder])]]): Task[Unit] =
    dao.markStocksUntracked(
      idWithResponses.collect {
        case Right((Some(id), _)) => id
      }
    )

  private def saleStocks(stocks: Seq[TrackStock]): Task[Unit] = for {
    idWithResponses <- sendRequestToSaleStocks(stocks)
    _ <- markSoldStocksUntrackedInDb(idWithResponses)
  } yield ()

  private def createNotifications(stocks: Seq[TrackStock]): Task[Unit] = Task(
    stocks collect {
      case TrackStock(Some(id), clientId, _, _, _, _, _) =>
        Notification(None, clientId = clientId, trackStockId = id)
    }).flatMap { notifications => dao.addNotifications(notifications) }

  private def getFigiPrices(figis: Seq[String]): Task[Map[String, Double]] =
    Task.parTraverseUnordered(figis) { figi =>
      getStockPrice(figi).value
    } map (
      _.collect {
        case Right((currentFigi, Some(price))) => (currentFigi, price)
      }.toMap
    )

  private def loop: Task[Unit] = for {
    trackedStocks <- dao.getAllTrackedStocks
    stockPricesMap <- getFigiPrices(trackedStocks.map(_.figi).distinct)
    stocksForSale = filterStocksForSale(trackedStocks, stockPricesMap)
    _ <- Task.parZip2(createNotifications(stocksForSale), saleStocks(stocksForSale))

    _ <- Task.now(logger.info({
      val sold = stocksForSale.size
      s"""Supported: ${trackedStocks.size - sold}, Sold: $sold"""
    }))
    result <- Task.defer(loop.delayExecution(10.seconds))
  } yield result

  def start: Task[Unit] = loop.delayExecution(5.seconds)
}
