package ru.zkerriga.investment

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import monix.eval.Task

import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.akkahttp.SwaggerAkka

import scala.concurrent.ExecutionContext

import logic.ServiceLogic
import tapir.TapirRoutes


case class Server()(implicit as: ActorSystem, ec: ExecutionContext) {

  val interface = "localhost"
  val port = 8080

  private val service = new ServiceLogic
  private val endpoints = new TapirRoutes(service)

  private val routes: Route = AkkaHttpServerInterpreter.toRoute(endpoints.all)

  private val openapi = OpenAPIDocsInterpreter.serverEndpointsToOpenAPI(
    endpoints.all, "investment control server", "0.0.1"
  )
  private val swagger = new SwaggerAkka(openapi.toYaml).routes


  def start: Task[Http.ServerBinding] = Task.fromFuture {
    Http()
      .newServerAt(interface, port)
      .bind(routes ~ swagger)
  } <* utils.Console.putAnyLn(s"Server started at: http://$interface:$port")

  def stop(http: Http.ServerBinding): Task[Done] = Task.fromFuture {
    http.unbind()
  } <* utils.Console.putAnyLn(s"Server stopped")

}
