package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpRequest, HttpResponse, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object SimpleHttpClient extends LazyLogging {
  def post[U: FromResponseUnmarshaller](uri: Uri, entity: RequestEntity = HttpEntity.Empty)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, POST)
    Http().singleRequest(HttpRequest(POST, uri = uri, entity = entity))
      .flatMap(response => Unmarshal(response).to[U])
  }

  def get[U: FromResponseUnmarshaller](uri: Uri)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, GET)
    Http().singleRequest(HttpRequest(GET, uri = uri))
      .andThen { case Success(response) => logResponse(response) }
      .flatMap(response => Unmarshal(response).to[U])
  }

  private def logRequest(uri: Uri, method: HttpMethod): Unit =
    logger.debug(s"Http request sent: ${method.value} $uri")

  private def logResponse(response: HttpResponse): Unit =
    logger.debug(s"Received response: code=${response.status}")
}
