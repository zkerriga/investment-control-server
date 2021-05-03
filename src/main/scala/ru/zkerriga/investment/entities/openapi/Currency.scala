package ru.zkerriga.investment.entities.openapi

import sttp.tapir._
import sttp.tapir.Codec.PlainCodec


sealed trait Currency
case object RUB extends Currency
case object USD extends Currency
case object EUR extends Currency
case object GBP extends Currency
case object HKD extends Currency
case object CHF extends Currency
case object JPY extends Currency
case object CNY extends Currency
case object TRY extends Currency

object Currency {
  implicit def plainCodecForCurrency: PlainCodec[Currency] = {
    Codec.string
      .map[Currency]((_: String) match {
        case "RUB" => RUB
        case "USD" => USD
        case "EUR" => EUR
        case "GBP" => GBP
        case "HKD" => HKD
        case "CHF" => CHF
        case "JPY" => JPY
        case "CNY" => CNY
        case "TRY" => TRY
      })(_.toString.toUpperCase)
        .validate(Validator.derivedEnum)
  }
  implicit def currencySchema: Schema[Currency] =
    Schema.string.validate(Validator.derivedEnum.encode(_.toString.toUpperCase))
}
