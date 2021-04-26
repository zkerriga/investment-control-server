package ru.zkerriga.investment.api

import ru.zkerriga.investment.entities.Login
import ru.zkerriga.investment.storage.ServerDatabase

import scala.concurrent.{ExecutionContext, Future}

class ServiceApiImpl extends ServiceApi {
  def registerClient(login: Login)(implicit ec: ExecutionContext): Future[Int] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
  }
}
