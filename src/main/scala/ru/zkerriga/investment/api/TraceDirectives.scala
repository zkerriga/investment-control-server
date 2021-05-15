package ru.zkerriga.investment.api

import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Route}
import akka.http.scaladsl.server.Directives.{extractClientIP, mapInnerRoute, mapResponse}
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.atomic.AtomicLong


trait TraceDirectives extends LazyLogging {

  private val counter: AtomicLong = new AtomicLong(0)

  private def count: Directive1[Long] = Directive { innerRouteSupplier =>
    ctx =>
      innerRouteSupplier(
        Tuple1(counter.incrementAndGet)
      )(ctx)
  }

  def log: Directive0 = count flatMap { requestId =>
    mapInnerRoute(addLoggingToRoute(requestId, _))
  }

  private def addLoggingToRoute(requestId: Long, innerRoute: Route): Route = {
    context => {
      extractClientIP { ip =>
        logger.info(s"Http request, id: $requestId, uri: ${context.request.uri}, forwarded ip: $ip")
        mapResponse(httpResponse => {
          logger.info(s"Http response, id: $requestId, code: ${httpResponse.status.intValue()}")
          httpResponse
        })(innerRoute)
      }(context)
    }
  }
}

object TraceDirectives extends TraceDirectives
