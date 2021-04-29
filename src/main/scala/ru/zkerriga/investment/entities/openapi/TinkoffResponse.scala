package ru.zkerriga.investment.entities.openapi


case class TinkoffResponse[A](trackingId: String, status: String, payload: A)
