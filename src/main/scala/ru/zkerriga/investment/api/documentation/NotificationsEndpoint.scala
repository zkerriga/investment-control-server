package ru.zkerriga.investment.api.documentation

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass

import ru.zkerriga.investment.entities.Notifications


trait NotificationsEndpoint {

  import sttp.tapir._
  import io.circe.generic.auto._
  import Authentication._

  private lazy val baseApiNotifications =
    ApiEndpoint.baseEndpoint
      .tag("notifications")
      .in("notifications")
      .withAuth

  private[api] lazy val getAllNotifications =
    baseApiNotifications.get
      .summary("Returns a list of unread notifications")
      .description("Retrieves stocks sale notifications from the database, returns them, and marks them as read")
      .in("all")
      .out(jsonBody[Notifications].description("Contains the number and list of notifications"))
}

object NotificationsEndpoint extends NotificationsEndpoint
