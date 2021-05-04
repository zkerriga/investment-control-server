package ru.zkerriga.investment

import ru.zkerriga.investment.entities.TinkoffToken


object TokenForTest {
  lazy val token: TinkoffToken = {
    val fromEnv = sys.env.get("TEST_TOKEN")
    fromEnv.fold(throw new RuntimeException("use `export TEST_TOKEN=****` for add token to environments")){
      TinkoffToken(_)
    }
  }
}
