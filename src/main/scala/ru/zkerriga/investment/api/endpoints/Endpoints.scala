package ru.zkerriga.investment.api.endpoints

import sttp.tapir.server.ServerEndpoint

trait Endpoints[F[_]] {
  def endpoints: List[ServerEndpoint[_, _, _, Any, F]]
}
