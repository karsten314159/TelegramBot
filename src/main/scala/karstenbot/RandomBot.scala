package karstenbot

import java.net.URLEncoder

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.clients.ScalajHttpClient

import scala.io.Source
import scala.util.Try
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

/** Generates random values.
  */
class RandomBot(val token: String, val googleKey: String) extends TelegramBot
  with Polling
  with Commands {
  val client = new ScalajHttpClient(token)
  val rng = new scala.util.Random(System.currentTimeMillis())

  def error(x: Any) = {
    println(x)
    None
  }

  onCommand("get") { implicit msg =>
    withArgs {
      case Seq(s) =>
        val str = s // .mkString(" ")
        println("Got: /get " + str)
        val url = "https://www.googleapis.com/customsearch/v1?key=" + googleKey + "&cx=003810642024859776485:k3cpkkhqqru&imgColorType=color&q=" + URLEncoder.encode(str, "UTF-8")
        println("... " + url)
        val src = Source.fromURL(url).mkString
        println("loaded")
        val js = JSON.parseRaw(src)
        println("parsed")
        val imgSrc =
          js match {
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
                                      Some(x)
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
        val res =
          if (imgSrc.isEmpty)
            "Sorry, nothing found for " + s
          else
            imgSrc.get + ""
        println("Res: " + res)
        reply(res)
      case _ => reply("Example: /get sherlock")
    }
  }

  /* Int(n) extractor */
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

}
