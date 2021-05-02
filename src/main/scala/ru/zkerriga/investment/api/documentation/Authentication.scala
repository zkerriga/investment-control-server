package ru.zkerriga.investment.api.documentation

import sttp.tapir.EndpointInput.WWWAuthenticate
import sttp.tapir.{Endpoint, auth}
import sttp.tapir.model.UsernamePassword
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass

import ru.zkerriga.investment.api.ExceptionResponse


object Authentication {
  private lazy val wwwAuth = WWWAuthenticate.basic("Enter the registration data")

  implicit class AuthenticationOps[O, R](val endpoint: Endpoint[Unit, Unit, O, R]) extends AnyVal {
    def withAuth: Endpoint[UsernamePassword, ExceptionResponse, O, R] =
      endpoint
        .in(auth.basic[UsernamePassword](wwwAuth))
        .errorOut(jsonBody[ExceptionResponse])
  }
}
