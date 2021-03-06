package ru.zkerriga.investment.api

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.redirect
import akka.http.scaladsl.server.Route
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.openapi.{Contact, Info, OpenAPI}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import scala.concurrent.Future

import ru.zkerriga.investment.api.endpoints.Endpoints
import ru.zkerriga.investment.configuration.ServerConf


class ServerRoutesImpl(endpoints: List[Endpoints[Future]], traceDirectives: TraceDirectives, config: ServerConf)
  extends ServerRoutes {

  import akka.http.scaladsl.server.RouteConcatenation._

  private val allEndpoints: List[ServerEndpoint[_, _, _, Any, Future]] = endpoints.flatten(e => e.endpoints)

  private val openapi: OpenAPI = OpenAPIDocsInterpreter.serverEndpointsToOpenAPI(
    allEndpoints,
    Info(
      title = "The Investment Control Server",
      version = "0.0.1",
      description = Some("Server for working with StopLoss and TakeProfit strategies via Tinkoff OpenAPI"),
      contact = Some(Contact(
        name = Some("Daniil Eletskii"),
        email = None,
        url = Some("https://github.com/zkerriga")
      ))
    )
  )

  private val contextPath: String = "docs"
  private val yamlName: String = "docs.yaml"
  private val swagger = new SwaggerAkka(openapi.toYaml, contextPath, yamlName).routes

  private val redirectToDocs: Route =
    redirect(s"/$contextPath", StatusCodes.PermanentRedirect)

  override def routes: Route =
    traceDirectives.log(AkkaHttpServerInterpreter.toRoute(allEndpoints)) ~ swagger ~ redirectToDocs
}
