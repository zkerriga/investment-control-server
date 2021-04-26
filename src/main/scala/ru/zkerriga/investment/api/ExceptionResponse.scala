package ru.zkerriga.investment.api

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class ExceptionResponse(errorMessage: String)

object ExceptionResponse {
  implicit val jsonEncoder: Encoder[ExceptionResponse] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExceptionResponse] = deriveDecoder
}
