package ru.zkerriga.investment.entities


case class VerifiedClient(
  id: Long,
  username: String,
  token: TinkoffToken,
)
