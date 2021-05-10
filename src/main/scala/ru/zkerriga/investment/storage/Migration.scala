package ru.zkerriga.investment.storage

import monix.eval.Task
import org.flywaydb.core.Flyway

import ru.zkerriga.investment.configuration.DatabaseConf


object Migration {

  def migrate(config: DatabaseConf): Task[Unit] = Task {
    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
  } map { flyway => flyway.migrate() }

}
