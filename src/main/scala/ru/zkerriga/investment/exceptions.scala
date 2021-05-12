package ru.zkerriga.investment

sealed abstract class ServiceException(message: String) extends Exception(message)

final case class LoginNotFound(login: String)
  extends ServiceException(s"Login `$login` not registered")

final case class LoginAlreadyExist(login: String)
  extends ServiceException(s"Login `$login` already exist")

final case class IncorrectCredentials()
  extends ServiceException("Incorrect login or password")

final case class InvalidToken()
  extends ServiceException("Invalid token")

final case class TokenDoesNotExist()
  extends ServiceException("A token is required to make the request")

final case class PageNotFound()
  extends ServiceException("Page not found")

final case class NotEnoughBalance()
  extends ServiceException("Not enough balance")

final case class ResponseError()

final case class InternalError(code: String)
  extends ServiceException(s"Internal error with code: $code")
