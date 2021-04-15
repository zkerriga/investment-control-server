package com.zkerriga.server
package akkahttp

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http

import scala.io.StdIn

object ServerEndpoint {
  val interface = "localhost"
  val port = 8080

  def main(args: Array[String]): Unit = {
    implicit val as = ActorSystem(Behaviors.empty, "my-system")
    implicit val ec = as.executionContext

    val endpoint = new ServerRoutes

    val bindingFuture =
      Http().newServerAt(interface, port)
        .bind(endpoint.routes)

    bindingFuture.foreach { s =>
      println(s"Server online at $s")
      println("Press RETURN to stop...")
    }

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => as.terminate()) // and shutdown when done
  }
}