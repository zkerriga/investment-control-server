package ru.zkerriga.investment

sealed abstract class ServiceException(message: String) extends Exception(message)

final case class LoginNotFound(login: String)
  extends ServiceException(s"Login `$login` not registered")

final case class LoginAlreadyExist(login: String)
  extends ServiceException(s"Login `$login` already exist")

final case class IncorrectCredentials()
  extends ServiceException(s"Incorrect login or password")
