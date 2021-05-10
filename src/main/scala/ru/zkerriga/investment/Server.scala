package ru.zkerriga.investment

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Route
import monix.eval.Task
import com.typesafe.scalalogging.LazyLogging

import ru.zkerriga.investment.api.ServerRoutes


case class Server(serverRoutes: ServerRoutes)(implicit as: ActorSystem) extends LazyLogging {

  private lazy val routes: Route = serverRoutes.routes

  def start(url: Uri): Task[Http.ServerBinding] = Task.deferFutureAction { implicit scheduler =>
    Http()
      .newServerAt(url.authority.host.address(), url.effectivePort)
      .bind(routes)
      .andThen { case _ =>
        logger.info(
          s"""Server started at: $url
             |See the documentation at: ${url.withPath(Uri.Path("/docs"))}""".stripMargin
        )
      }
    }


  def stop(http: Http.ServerBinding): Task[Done] = Task.deferFutureAction { implicit scheduler =>
    http
      .unbind()
      .andThen { case _ =>
        logger.info("Server stopped.")
      }
  }

}
