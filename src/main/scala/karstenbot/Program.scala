package karstenbot

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Program extends App {
  val sec = SecretTrait.impl
  val botStart = true

  if (botStart) {
    val bot = new KarstenBot(sec)
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
}
