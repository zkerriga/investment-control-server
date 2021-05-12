package ru.zkerriga.investment.api.documentation

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.model.UsernamePassword

import ru.zkerriga.investment.entities.{Login, TinkoffToken}
import ru.zkerriga.investment.api.ExceptionResponse


trait RegisterEndpoint {

  import sttp.tapir._
  import Authentication._

  private lazy val baseApiRegistration =
    ApiEndpoint.baseEndpoint
      .tag("registration")

  private[api] lazy val register: Endpoint[Login, ExceptionResponse, Unit, Any] =
    baseApiRegistration.post
      .summary("Registers a client")
      .description("Checks if the username is free and registers a new client")
      .in("register")
      .in(jsonBody[Login])
      .errorOut(jsonBody[ExceptionResponse].description("The login may be busy"))

  private[api] lazy val updateToken: Endpoint[(UsernamePassword, TinkoffToken), ExceptionResponse, Unit, Any] =
    baseApiRegistration.put.withAuth
      .summary("Updates the token")
      .description("Checks the validity of the token from Tinkoff-OpenAPI and enters it in the client data")
      .in("update" / "token")
      .in(jsonBody[TinkoffToken])

}

object RegisterEndpoint extends RegisterEndpoint
