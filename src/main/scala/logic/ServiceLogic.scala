package com.zkerriga.server
package logic

import monix.eval.Task
import database.ServerDatabase

import domain.Login


class ServiceLogic() {
  def registerClient(login: Login): Task[Int] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
  }
}
