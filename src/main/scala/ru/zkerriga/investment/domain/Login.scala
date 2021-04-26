package ru.zkerriga.investment.domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


case class Login(login: String, password: String)

object Login {
  implicit val jsonDecoder: Decoder[Login] = deriveDecoder
  implicit val jsonEncoder: Encoder[Login] = deriveEncoder
}
