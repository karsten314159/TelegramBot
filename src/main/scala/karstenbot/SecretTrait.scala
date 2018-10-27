package karstenbot

trait SecretTrait {
  val geoUser: String

  val telegramToken: String
  val googleKey: String
  val cx: String

  val dbHost: String
  val dbName: String
  val dbUser: String
  val dbPw: String
  val dbPort: Int
}

object SecretTrait {
  var impl: SecretTrait = Secret
}