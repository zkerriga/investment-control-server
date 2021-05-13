package ru.zkerriga.investment.utils

import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import com.github.t3hnar.bcrypt.BCryptStrOps
import scala.concurrent.blocking


trait AsyncBcrypt {
  def hash(password: String, rounds: Int = 12): Task[String]
  def verify(password: String, hash: String): Task[Boolean]
}

class AsyncBcryptImpl extends AsyncBcrypt with StrictLogging {

  override def hash(password: String, rounds: Int): Task[String] = Task {
    blocking(password.bcryptBounded(rounds))
  }

  override def verify(password: String, hash: String): Task[Boolean] = Task {
    blocking(password.isBcryptedBounded(hash))
  }

}
