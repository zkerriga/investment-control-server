package ru.zkerriga.investment.routes

import akka.http.scaladsl.server.Route
import sttp.tapir.openapi.OpenAPI


trait ServerRoutes {
  def routes: Route
  def openapi: OpenAPI
}
