package ru.zkerriga.investment.newroutes.documentation

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

  private[api] lazy val register: Endpoint[Login, ExceptionResponse, String, Any] =
    baseApiRegistration
      .summary("Registers a client")
      .description("Checks if the username is free and registers a new client")
      .in("register")
      .in(jsonBody[Login])
      .out(jsonBody[String].description("Returns the username in case of successful registration"))
      .errorOut(jsonBody[ExceptionResponse].description("The login may be busy"))

  private[api] lazy val updateToken: Endpoint[(UsernamePassword, TinkoffToken), ExceptionResponse, String, Any] =
    baseApiRegistration.withAuth
      .summary("Updates the token")
      .description("Checks the validity of the token from Tinkoff-OpenAPI and enters it in the client data")
      .in("update" / "token")
      .in(jsonBody[TinkoffToken])
      .out(jsonBody[String].description("Returns the username in case of successful registration"))
}

object RegisterEndpoint extends RegisterEndpoint
