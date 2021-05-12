package ru.zkerriga.investment.exceptions


sealed abstract class InternalError(message: String) extends Throwable

final case class DatabaseError()
  extends InternalError("Database request error")

final case class OpenApiResponseError(response: String)
  extends InternalError(s"OpenAPI request error: $response")
