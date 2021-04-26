package ru.zkerriga.investment.api

import monix.eval.Task

import ru.zkerriga.investment.LoginAlreadyExist
import ru.zkerriga.investment.entities.Login
import ru.zkerriga.investment.storage.ServerDatabase


class ServiceApiImpl extends ServiceApi {
  def registerClient(login: Login): Task[String] = {
    val hash = util.hashing.MurmurHash3.stringHash(login.password)
    ServerDatabase.registerClient(login.login, hash.toString)
      .map(_ => login.login)
      .onErrorFallbackTo(Task.raiseError(LoginAlreadyExist(login.login)))
  }
}
