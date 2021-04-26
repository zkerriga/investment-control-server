package ru.zkerriga.investment.akkahttp

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route

class ServerRoutes {
  import akka.http.scaladsl.server.Directives._

  val routes: Route = get {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    }
}
