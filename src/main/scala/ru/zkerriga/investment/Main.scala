package ru.zkerriga.investment

import akka.actor.ActorSystem
import monix.eval.Task

import logging.Console


object Main {
  implicit val as: ActorSystem = ActorSystem()
  import monix.execution.Scheduler.Implicits.global

  private def terminateSystem = Task.fromFuture(as.terminate())

  def main(args: Array[String]): Unit = {
    val server = Server("localhost", 8080)
    val program = for {
      http <- server.start
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- terminateSystem
    } yield ()

    program.runSyncUnsafe()
  }

}
