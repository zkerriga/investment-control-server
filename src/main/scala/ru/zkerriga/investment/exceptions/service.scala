package ru.zkerriga.investment.exceptions

sealed abstract class ServiceError(message: String) extends Throwable

sealed abstract class AuthenticationError(message: String)
  extends ServiceError(message)

sealed abstract class MarketError(message: String)
  extends ServiceError(message)


final case class UsernameNotFound(login: String)
  extends AuthenticationError(s"Login `$login` not registered")

final case class UsernameAlreadyExist(username: String)
  extends AuthenticationError(s"Login `$username` already exist")

final case class IncorrectCredentials()
  extends AuthenticationError("Incorrect login or password")

final case class InvalidToken()
  extends AuthenticationError("Invalid token")

final case class TokenDoesNotExist()
  extends AuthenticationError("A token is required to make the request")


final case class PageNotFound()
  extends MarketError("Page not found")

final case class NotEnoughBalance()
  extends MarketError("Not enough balance")
