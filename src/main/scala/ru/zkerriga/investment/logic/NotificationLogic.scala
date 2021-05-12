package ru.zkerriga.investment.logic

import cats.data.EitherT
import monix.eval.Task

import ru.zkerriga.investment.entities.{Notifications, VerifiedClient}
import ru.zkerriga.investment.exceptions.DatabaseError


trait NotificationLogic {

  def getAllNotifications(client: VerifiedClient): EitherT[Task, DatabaseError, Notifications]

}
