package ru.zkerriga.investment.storage

import monix.eval.Task
import slick.jdbc.PostgresProfile.backend.Database
import scala.concurrent.Future

import ru.zkerriga.investment.configuration.DatabaseConf


object DatabaseHelper {

  def createDb(config: DatabaseConf): Task[Database] = Task {
    Database.forURL(
      url = config.url,
      user = config.user,
      password = config.password,
      driver = config.driver
    )
  }

  def createQueryRunner(db: Database): QueryRunner[Task] = new QueryRunner[Task](db) {
    override protected def converter[A]: Future[A] => Task[A] =
      future => Task.deferFutureAction { implicit scheduler => future }
  }

}
