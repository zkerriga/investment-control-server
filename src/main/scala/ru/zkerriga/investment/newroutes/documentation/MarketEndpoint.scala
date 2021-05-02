package ru.zkerriga.investment.newroutes.documentation

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.openapi.Stocks
import ru.zkerriga.investment.api.ExceptionResponse


trait MarketEndpoint {

  import sttp.tapir._
  import Authentication._

  private lazy val baseApiMarket =
    ApiEndpoint.baseEndpoint
      .tag("market")
      .withAuth

  private[api] lazy val stocks: Endpoint[(UsernamePassword, Int, Int), ExceptionResponse, Stocks, Any] =
    baseApiMarket.get
      .summary("Get a list of stocks")
      .description("The request goes to Tinkoff-OpenAPI and gets a part of the list of available stocks")
      .in("market" / "stocks")
      .in(
        query[Int]("page").default(1).description("Page with stocks") and
        query[Int]("onPage").default(20).description("So many stocks will be on one page")
      )
      .out(jsonBody[Stocks])
}

object MarketEndpoint extends MarketEndpoint
