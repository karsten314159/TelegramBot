package karstenbot

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Program {}

object Program extends App {
  val googleKey = Secret.googleKey
  val tok = Secret.telegramToken
  val cx = Secret.cx

  val bot = new KarstenBot(tok, googleKey, cx)
  val eol = bot.run()
  println("Started :)")
  println("https://telegram.me/karsten314159bot")
  println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
  scala.io.StdIn.readLine()
  bot.shutdown() // initiate shutdown
  // Wait for the bot end-of-life
  Await.result(eol, Duration.Inf)
  println("Done")
}