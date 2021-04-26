package ru.zkerriga.investment.logic

import ru.zkerriga.investment.storage.ServerDatabase
import ru.zkerriga.investment.entities.Login

import scala.concurrent.{ExecutionContext, Future}


class ServiceLogic() {
  def registerClient(login: Login)(implicit ec: ExecutionContext): Future[Int] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
  }
}
