package ru.zkerriga.investment.newroutes.documentation

import sttp.tapir.{Endpoint, EndpointInput, _}

trait ApiEndpoint {
  lazy val apiResource: String = "api"
  lazy val apiVersion: String = "v1"
  lazy val apiName: String = "investment"

  private[api] lazy val baseApiEndpointInput: EndpointInput[Unit] =
    apiResource / apiVersion / apiName

  private[api] lazy val baseEndpoint: Endpoint[Unit, Unit, Unit, Any] =
    endpoint
      .in(baseApiEndpointInput)
      .name("name test")
      .description("description test")
}

object ApiEndpoint extends ApiEndpoint
