package com.zkerriga.server
package logic

import monix.eval.Task
import database.ServerDatabase
import domain.Login

import scala.concurrent.{ExecutionContext, Future}


class ServiceLogic() {
  def registerClient(login: Login)(implicit ec: ExecutionContext): Future[Int] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
  }
}
