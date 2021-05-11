package ru.zkerriga.investment

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import monix.eval.Task
import com.typesafe.scalalogging.LazyLogging

import ru.zkerriga.investment.api.ServerRoutes
import ru.zkerriga.investment.configuration.ServerConf


case class Server(serverRoutes: ServerRoutes)(implicit as: ActorSystem) extends LazyLogging {

  private lazy val routes: Route = serverRoutes.routes

  def start(config: ServerConf): Task[Http.ServerBinding] = Task.deferFutureAction { implicit scheduler =>
    Http()
      .newServerAt(config.host, config.port.number)
      .bind(routes)
      .andThen { case _ =>
        logger.info(
          s"""Server started at: ${ServerConf.getUri(config)}
             |See the documentation at: ${ServerConf.getUri(config)}/docs""".stripMargin
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
