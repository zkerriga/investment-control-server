package ru.zkerriga.investment.entities.openapi

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class Stocks(total: Int, instruments: Seq[Stock])

object Stocks {
  implicit val jsonDecoder: Decoder[Stocks] = deriveDecoder
  implicit val jsonEncoder: Encoder[Stocks] = deriveEncoder
}
