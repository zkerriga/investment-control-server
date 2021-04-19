package com.zkerriga.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import monix.eval.Task

import scala.concurrent.ExecutionContext
import akkahttp.ServerRoutes

case class Server()(implicit as: ActorSystem, ec: ExecutionContext) {

  val interface = "localhost"
  val port = 8080

  private val routes: Route = (new ServerRoutes).routes /* todo: change to real routes */

  def start: Task[Http.ServerBinding] = Task.fromFuture {
    Http()
      .newServerAt(interface, port)
      .bind(routes)
  } <* utils.Console.putAnyLn(s"Server started at: http://$interface:$port")

  def stop(http: Http.ServerBinding): Task[Done] = Task.fromFuture {
    http.unbind()
  } <* utils.Console.putAnyLn(s"Server stopped")

}
