package ru.zkerriga.investment.entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/**
 * The token must be linked to a brokerage account
 * in the Tinkoff Investments service.
 */
case class TinkoffToken(token: String)

object TinkoffToken {
  implicit val jsonDecoder: Decoder[TinkoffToken] = deriveDecoder
  implicit val jsonEncoder: Encoder[TinkoffToken] = deriveEncoder
}
