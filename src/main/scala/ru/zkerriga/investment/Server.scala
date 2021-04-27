package ru.zkerriga.investment

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import com.typesafe.scalalogging._
import api.{ServiceApi, ServiceApiImpl}
import routes.TapirRoutes


case class Server(interface: String, port: Int)(implicit as: ActorSystem, s: Scheduler) extends LazyLogging {

  val link = s"http://$interface:$port"

  private val service: ServiceApi = new ServiceApiImpl
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
      .andThen{ case _ =>
        logger.info(
          s"""Server started at: $link
             |See the documentation at: $link/docs""".stripMargin
        )
      }
  }

  def stop(http: Http.ServerBinding): Task[Done] = Task.fromFuture {
    http
      .unbind()
      .andThen{ case _ =>
        logger.info("Server stopped.")
      }
  }

}
