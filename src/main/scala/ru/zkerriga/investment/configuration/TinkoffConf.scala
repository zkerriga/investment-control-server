package ru.zkerriga.investment.configuration

import ru.zkerriga.investment.entities.openapi.SandboxSetCurrencyBalanceRequest


case class TinkoffConf(
  url: String,
  startBalance: SandboxSetCurrencyBalanceRequest,
  token: String,
)
