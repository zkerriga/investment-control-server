package ru.zkerriga.investment.api

import akka.http.scaladsl.server.Route


trait ServerRoutes {
  def routes: Route
}
