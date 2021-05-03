package ru.zkerriga.investment.storage

import org.scalatest.matchers.should.Matchers
import ClientsQueryRepository._


class ClientsQueryRepositoryTest extends ClientsDatabaseSuite with Matchers {

  test("add new client should return 1") {
    for {
      res <- addClient("login3", "hash:something3")
    } yield assert(res >= 0L)
  }

  test("found existed client") {
    for {
      found <- findByLogin("login1")
    } yield assert(found.fold(false)(client => client.login === "login1"))
    for {
      found <- findById(1)
    } yield assert(found.fold(false)(client => client.login === "login1"))
  }

  test("inactive client") {
    for {
      _ <- inactiveClient(1)
      found <- findByLogin("login1")
    } yield assert(found.fold(false)(_.active === false))
  }

  test("update token") {
    for {
      _ <- updateToken(1, "token:something1")
      found <- findByLogin("login1")
    } yield assert(found.fold(false)(_.token === Some("token:something1")))

    for {
      _ <- updateToken(2, "token:new2")
      found <- findByLogin("login2")
    } yield assert(found.fold(false)(_.token === Some("token:new2")))
  }
}
