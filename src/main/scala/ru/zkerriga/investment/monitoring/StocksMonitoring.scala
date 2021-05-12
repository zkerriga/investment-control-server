package ru.zkerriga.investment.monitoring

import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import scala.concurrent.duration._

import ru.zkerriga.investment.entities.TinkoffToken
import ru.zkerriga.investment.entities.openapi.{MarketOrderRequest, PlacedMarketOrder, TinkoffResponse}
import ru.zkerriga.investment.exceptions.{DatabaseError, OpenApiResponseError}
import ru.zkerriga.investment.logic.OpenApiClient
import ru.zkerriga.investment.storage.MonitoringDao
import ru.zkerriga.investment.storage.entities.{Notification, TrackStock}

/* todo: make test with private[monitoring] */
class StocksMonitoring(openApiClient: OpenApiClient, dao: MonitoringDao, token: TinkoffToken) extends LazyLogging {

  type FIGI = String
  type Price = Double
  type Id = Option[Long]
  type Response = TinkoffResponse[PlacedMarketOrder]

  private def getStockPrice(figi: FIGI): EitherT[Task, OpenApiResponseError, (FIGI, Option[Price])] =
    openApiClient.`/market/orderbook`(token, figi) map {
      case TinkoffResponse(_, _, orderBook) => (figi, orderBook.lastPrice)
    }

  private def filterStocksForSale(stocks: Seq[TrackStock], stockPrices: Map[FIGI, Price]): Seq[TrackStock] =
    stocks.filter { stock =>
      stockPrices.get(stock.figi).fold(false) { price =>
        price >= stock.takeProfit || price <= stock.stopLoss
      }
    }

  private def sendRequestToSaleStocks(stocks: Seq[TrackStock]): Task[Seq[Either[OpenApiResponseError, (Id, Response)]]] =
    Task.parTraverseUnordered(stocks) { stock =>
      openApiClient.`/orders/market-order`(
        token,
        stock.figi,
        MarketOrderRequest(stock.lots, "Sell")
      ).map(stock.id -> _).value
    }

  private def markSoldStocksUntrackedInDb(idWithResponses: Seq[Either[OpenApiResponseError, (Id, Response)]]): EitherT[Task, DatabaseError, Unit] =
    dao.markStocksUntracked(
      idWithResponses.collect {
        case Right((Some(id), _)) => id
      }
    )

  private def saleStocks(stocks: Seq[TrackStock]): EitherT[Task, DatabaseError, Unit] =
    for {
      idWithResponses <- EitherT.right(sendRequestToSaleStocks(stocks))
      _ <- markSoldStocksUntrackedInDb(idWithResponses)
    } yield ()

  private def convertToNotifications(stocks: Seq[TrackStock]): Seq[Notification] =
    stocks collect {
      case TrackStock(Some(id), clientId, _, _, _, _, _) =>
        Notification(None, clientId = clientId, trackStockId = id)
    }

  private def addNotificationsToDb(notifications: Seq[Notification]): EitherT[Task, DatabaseError, Unit] =
    dao.addNotifications(notifications)

  private def getStockPrices(figiSeq: Seq[FIGI]): Task[Map[FIGI, Price]] =
    Task.parTraverseUnordered(figiSeq) { figi =>
      getStockPrice(figi).value
    } map (
      _.collect {
        case Right((currentFigi, Some(price))) => (currentFigi, price)
      }.toMap
    )

  private def parAddNotificationsAndSale(notifications: Seq[Notification], stocks: Seq[TrackStock]): EitherT[Task, DatabaseError, Unit] =
    EitherT(Task.parZip2(addNotificationsToDb(notifications).value, saleStocks(stocks).value) map {
      case (notificationEither, stocksEither) => for {
        _ <- notificationEither
        _ <- stocksEither
      } yield ()
    })

  private def logInfo(trackedQuantity: Int, soldQuantity: Int): Task[Unit] =
    Task.now(
      logger.info(s"""Supported: ${trackedQuantity - soldQuantity}, Sold: $soldQuantity""")
    )

  private def loop: EitherT[Task, DatabaseError, Unit] = for {
    trackedStocks   <- dao.getAllTrackedStocks
    stockPricesMap  <- EitherT.right(getStockPrices(trackedStocks.map(_.figi).distinct))

    stocksForSale = filterStocksForSale(trackedStocks, stockPricesMap)
    notifications = convertToNotifications(stocksForSale)

    _ <- parAddNotificationsAndSale(notifications, stocksForSale)
    _ <- EitherT.right(logInfo(trackedStocks.size, stocksForSale.size))
    result <- EitherT(Task.defer(loop.value.delayExecution(10.seconds)))
  } yield result

  def start: Task[Unit] =
    loop
      .valueOrF(Task.raiseError)
      .delayExecution(5.seconds)
}
