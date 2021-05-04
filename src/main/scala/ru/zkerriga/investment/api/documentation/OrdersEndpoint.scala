package ru.zkerriga.investment.api.documentation

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass
import ru.zkerriga.investment.entities.StockOrder
import ru.zkerriga.investment.entities.openapi.Order


trait OrdersEndpoint {

  import sttp.tapir._
  import io.circe.generic.auto._
  import Authentication._

  private lazy val baseApiOrders =
    ApiEndpoint.baseEndpoint
      .tag("orders")
      .in("orders")
      .withAuth

  private[api] lazy val marketOrder =
    baseApiOrders.post
      .summary("Creating a market buy-order")
      .description("Creates a market bid to buy stocks using Tinkoff-OpenAPI")
      .in("market-order" / "buy")
      .in(jsonBody[StockOrder])
      .out(jsonBody[Order].description("All information about the created bid"))
}

object OrdersEndpoint extends OrdersEndpoint
