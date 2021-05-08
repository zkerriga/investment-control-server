package ru.zkerriga.investment

import monix.eval.Task

import ru.zkerriga.investment.entities.TinkoffToken


object TokenForServer {
  private lazy val errorMessage = "Use `export SERVER_TOKEN=****` for add token to environments!"

  def token: Task[TinkoffToken] = {
    val fromEnv = sys.env.get("SERVER_TOKEN")
    fromEnv.fold(Task.raiseError[TinkoffToken](new RuntimeException(errorMessage))) { t =>
      Task.now(TinkoffToken(t))
    }
  }
}
