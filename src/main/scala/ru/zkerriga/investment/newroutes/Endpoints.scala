package ru.zkerriga.investment.newroutes

import sttp.tapir.server.ServerEndpoint


trait Endpoints[F[_]] {
  def endpoints: List[ServerEndpoint[_, _, _, Any, F]]
}
