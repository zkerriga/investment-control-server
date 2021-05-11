package ru.zkerriga.investment.logic

import monix.eval.Task

import ru.zkerriga.investment.entities.{Notifications, VerifiedClient}


trait NotificationLogic {

  /**
   * Accesses the database and generates a list of notifications for the client
   * @return all notifications for the client
   */
  def getAllNotifications(client: VerifiedClient): Task[Notifications]

}
