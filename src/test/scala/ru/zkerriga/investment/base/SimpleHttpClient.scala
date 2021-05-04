package ru.zkerriga.investment.base

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST, PUT}
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpRequest, HttpResponse, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object SimpleHttpClient extends LazyLogging {
  def postWithoutCreds[U: FromResponseUnmarshaller](uri: Uri, entity: RequestEntity = HttpEntity.Empty)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, POST)
    Http().singleRequest(HttpRequest(POST, uri = uri, entity = entity))
      .flatMap(response => Unmarshal(response).to[U])
  }

  def post[U: FromResponseUnmarshaller](uri: Uri, entity: RequestEntity = HttpEntity.Empty, credentials: HttpCredentials)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, POST)
    Http().singleRequest(HttpRequest(POST, uri = uri, entity = entity).addCredentials(credentials))
      .flatMap(response => Unmarshal(response).to[U])
  }

  def put[U: FromResponseUnmarshaller](uri: Uri, entity: RequestEntity, credentials: HttpCredentials)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, PUT)
    Http().singleRequest(HttpRequest(PUT, uri = uri, entity = entity).addCredentials(credentials))
      .flatMap(response => Unmarshal(response).to[U])
  }

  def get[U: FromResponseUnmarshaller](uri: Uri, credentials: HttpCredentials)(implicit ac: ActorSystem, ec: ExecutionContext): Future[U] = {
    logRequest(uri, GET)
    Http().singleRequest(HttpRequest(GET, uri = uri).addCredentials(credentials))
      .andThen { case Success(response) => logResponse(response) }
      .flatMap(response => Unmarshal(response).to[U])
  }

  private def logRequest(uri: Uri, method: HttpMethod): Unit =
    logger.info(s"Http request sent: ${method.value} $uri")

  private def logResponse(response: HttpResponse): Unit =
    logger.debug(s"Received response: code=${response.status}")
}
