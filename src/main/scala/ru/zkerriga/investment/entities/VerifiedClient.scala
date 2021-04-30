package ru.zkerriga.investment.entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import ru.zkerriga.investment.storage.Client


case class VerifiedClient(
  id: Long,
  login: String,
  token: TinkoffToken,
)

object VerifiedClient {
  implicit val jsonDecoder: Decoder[VerifiedClient] = deriveDecoder
  implicit val jsonEncoder: Encoder[VerifiedClient] = deriveEncoder

  def fromClient(client: Client, token: TinkoffToken): VerifiedClient =
    VerifiedClient(
      id = client.id,
      login = client.login,
      token = token
    )
}
