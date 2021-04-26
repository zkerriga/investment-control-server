package ru.zkerriga.investment.api

import monix.eval.Task

import ru.zkerriga.investment.LoginAlreadyExist
import ru.zkerriga.investment.entities.Login
import ru.zkerriga.investment.storage.ServerDatabase


class ServiceApiImpl extends ServiceApi {
  def registerClient(login: Login): Task[Int] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
      .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.login)))
  }
}
