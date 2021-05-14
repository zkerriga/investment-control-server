# The Investment Control Server
Server for working with StopLoss and TakeProfit strategies via
[Tinkoff-OpenAPI](https://tinkoffcreditsystems.github.io/invest-openapi/swagger-ui/). Educational project.

### Problem
Even a strong investor can find it difficult to keep track of all the events in the market that may affect his assets.
Sometimes the stock price can drop by 50% in a couple of minutes while the investor is busy with something else.

### Solution
Trust the stock price tracking algorithm!

This server will help you buy stocks with StopLoss and TakeProfit prices and will constantly poll
[Tinkoff-OpenAPI](https://tinkoffcreditsystems.github.io/invest-openapi/swagger-ui/) to sell stocks
in case of non-compliance with your condition.

### More detailed
The server is able to work with several clients at once. Each client is able to:
* Register
* Update your token from Tinkoff Investments
* Get a list of stocks
* Buy stocks with price limits specified
* Receive notifications about sold stocks

For more information about each request, see the documentation for the relative link `/docs`, 
which will be available from the running server.

### Technologies used
* akka-http - for processing HTTP requests
* tapir - to generate requests paths and documentation
* monix - for asynchronous operation of server internals
* slick - to create SQL queries and connect to PostgreSQL
* circe - for processing JSON bodies
* pureconfig - to work with the configuration
* bcrypt - to encrypt user passwords
* flyway - for database migration

### How to run?
1. Configure the PostgreSQL database and enter the *url*, *user*, and *password* in the [application.conf](./src/main/resources/application.conf)
2. Get a *token* in your personal account of Tinkoff Investments and add it to the [application.conf](./src/main/resources/application.conf)
3. Compile and run the server: `sbt compile && sbt run`
