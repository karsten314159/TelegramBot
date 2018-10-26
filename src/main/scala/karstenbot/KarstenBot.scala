package karstenbot

import java.net.URLEncoder

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.models.Message

import scala.concurrent.Future
import scala.io.{Codec, Source}
import scala.util.Try
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

class KarstenBot(val token: String, val googleKey: String, val cx: String) extends TelegramBot
  with Polling
  with Commands {
  val client = new ScalajHttpClient(token)
  val rng = new scala.util.Random(System.currentTimeMillis())

  def error[T](x: Any): Option[T] = {
    println(x)
    None
  }

  /** https://alvinalexander.com/scala/how-to-write-scala-http-get-request-client-source-fromurl */
  def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET"): String = {
    import java.net.{HttpURLConnection, URL}
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream)(Codec.UTF8).mkString
    if (inputStream != null) inputStream.close()
    content
  }

  def actionOn(implicit msg: Message): Future[Message] = {
    val str = msg.text.getOrElse("")
    println("Got message from " + msg.chat.username.getOrElse("") + " (" + msg.chat.firstName.getOrElse("") + "): " + str)

    val url = "https://www.googleapis.com/customsearch/v1?key=" + googleKey + "&cx=" + cx + "&imgColorType=color&q=" + URLEncoder.encode(str, "UTF-8")
    println(">  getting " + url)

    val src = Try(get(url))
    println("> loaded")

    val res =
      if (src.isFailure) {
        println("> timeout or error: " + src.failed.get)
        "Sorry, cannot find an image in time for " + str
      } else {
        val imgSrc = parseGoogleJson(src.get)
        println("> parsed")
        if (imgSrc.isEmpty)
          "Sorry, search returned nothing for " + str
        else
          imgSrc.get + ""
      }

    println("> Res: " + res)
    reply(res)
  }

  onEditedMessage { implicit msg =>
    actionOn
  }

  onMessage { implicit msg =>
    actionOn
  }

  def parseGoogleJson(src: String): Option[String] = {
    JSON.parseRaw(src) match {
      case Some(x: JSONObject) =>
        x.obj.find(_._1 == "items") match {
          case Some((_, x: JSONArray)) =>
            x.list.headOption match {
              case Some(x: JSONObject) =>
                x.obj.find(_._1 == "pagemap") match {
                  case Some((_, x: JSONObject)) =>
                    x.obj.find(_._1 == "cse_image") match {
                      case Some((_, x: JSONArray)) =>
                        x.list.headOption match {
                          case Some(x: JSONObject) =>
                            x.obj.find(_._1 == "src") match {
                              case Some((_, x)) =>
                                Some("" + x)
                              case x => error(x)
                            }
                          case x => error(x)
                        }
                      case x => error(x)
                    }
                  case x => error(x)
                }
              case x => error(x)
            }
          case x => error(x)
        }
      case x => error(x)
    }
  }
}