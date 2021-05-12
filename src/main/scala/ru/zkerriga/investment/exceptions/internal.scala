package ru.zkerriga.investment.exceptions


sealed abstract class ServerInternalError(message: String) extends Throwable

final case class DatabaseError()
  extends ServerInternalError("Database request error")

final case class OpenApiResponseError(response: String)
  extends ServerInternalError(s"OpenAPI request error: $response")

final case class ProgrammedError(message: String)
  extends ServerInternalError(message)
