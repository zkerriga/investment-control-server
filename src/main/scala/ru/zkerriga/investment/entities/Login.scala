package ru.zkerriga.investment.entities

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


/**
 * To transfer registration information or verify an existing account.
 * @param username a client's username
 * @param password a client's password (without encryption)
 */
case class Login(username: String, password: String)

object Login {
  implicit val jsonDecoder: Decoder[Login] = deriveDecoder
  implicit val jsonEncoder: Encoder[Login] = deriveEncoder
}
