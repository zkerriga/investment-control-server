package ru.zkerriga.investment.logging

import monix.eval.Task

import scala.io.StdIn

object Console {
  def readLine: Task[String] = Task(StdIn.readLine())
  def putAnyLn(any: Any): Task[Unit] = Task(println(any))
}
