package ru.zkerriga.investment.entities.openapi

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


case class Stock(
  figi: String,
  ticker: String,
  isin: String,
  minPriceIncrement: Option[Double],
  lot: Int,
  minQuantity: Option[Int],
  currency: String,
  name: String
)

object Stock {
  implicit val jsonDecoder: Decoder[Stock] = deriveDecoder
  implicit val jsonEncoder: Encoder[Stock] = deriveEncoder
}
