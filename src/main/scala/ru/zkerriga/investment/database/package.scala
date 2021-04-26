package ru.zkerriga.investment

import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

package object database {
  type DIO[+R, -E <: Effect] = DBIOAction[R, NoStream, E]
  val DIO = DBIO
}
