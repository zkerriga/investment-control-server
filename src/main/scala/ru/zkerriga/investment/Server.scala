package ru.zkerriga.investment

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import monix.eval.Task
import monix.execution.Scheduler
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import com.typesafe.scalalogging._
import akka.http.scaladsl.server.RouteConcatenation._

import ru.zkerriga.investment.routes.ServerRoutes


case class Server(serverRoutes: ServerRoutes)(implicit as: ActorSystem, s: Scheduler) extends LazyLogging {

  private val routes: Route = serverRoutes.routes
  private val swagger = new SwaggerAkka(serverRoutes.openapi.toYaml).routes

  def start(interface: String, port: Int): Task[Http.ServerBinding] = Task.fromFuture {
    val link = s"http://$interface:$port"
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
