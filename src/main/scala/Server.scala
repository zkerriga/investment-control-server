package com.zkerriga.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task

import scala.concurrent.ExecutionContext
import akkahttp.ServerRoutes

case class Server()(implicit as: ActorSystem, ec: ExecutionContext) extends LazyLogging {

  private val routes: Route = (new ServerRoutes).routes /* todo: change to real routes */

  def start: Task[Http.ServerBinding] = Task.fromFuture {
    Http()
      .newServerAt("localhost", 8080)
      .bind(routes)
      .andThen { s => logger.info(s"Server started at: $s") }
  }

  def stop(http: Http.ServerBinding): Task[Done] = Task.fromFuture {
    http
      .unbind()
      .andThen { _ => logger.info("Server stopped") }
  }

}
