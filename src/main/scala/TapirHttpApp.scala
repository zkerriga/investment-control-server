package com.zkerriga.server

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import monix.eval.Task

import utils.Console


object TapirHttpApp {
  implicit val as: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = as.dispatcher

  private def terminateSystem = Task.fromFuture(as.terminate())

  def main(args: Array[String]): Unit = {
    val server = Server()
    val program = for {
      http <- server.start
      _ <- Console.putAnyLn("Press RETURN to stop server...")
      _ <- Console.readLine
      _ <- server.stop(http)
      _ <- terminateSystem
    } yield ()

    import monix.execution.Scheduler.Implicits.global
    program.runSyncUnsafe()
  }

}
