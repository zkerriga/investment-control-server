package ru.zkerriga.investment

import akka.actor.ActorSystem
import monix.eval.Task
import logging.Console
import monix.execution.Scheduler


object Main {
  implicit val as: ActorSystem = ActorSystem()
  implicit val s: Scheduler = monix.execution.Scheduler.global

  private def terminateSystem = Task.fromFuture(as.terminate())

  def main(args: Array[String]): Unit = {
    val server = Server()
    val program = for {
      http <- server.start("localhost", 8080)
      _ <- Console.putAnyLn("Press ENTER to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- terminateSystem
    } yield ()

    program.runSyncUnsafe()
  }

}
