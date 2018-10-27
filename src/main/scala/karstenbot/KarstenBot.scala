package karstenbot

import java.net.URLEncoder

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.models._

import scala.concurrent.Future
import scala.io.{Codec, Source}
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}
import scala.util.{Failure, Random, Success, Try}

class KarstenBot(val sec: SecretTrait) extends TelegramBot
  with Polling
  with Commands {
  val client = new ScalajHttpClient(sec.telegramToken)
  val rng = new Random(System.currentTimeMillis)
  val db = new Database(sec)
  if (!db.test) {
    sys.error("DB not accessible: " + db.url)
  }

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

  val n: String = "North"
  val w: String = "West"
  val e: String = "East"
  val s: String = "South"
  val start: String = "/start"

  def actionOn(implicit msg: Message): Future[Message] = {
    val str = msg.text.getOrElse("")
    println("Got message from " + msg.chat.username.getOrElse("") + " (" + msg.chat.firstName.getOrElse("") + "): " + str)

    val url = "https://www.googleapis.com/customsearch/v1?key=" + sec.googleKey + "&cx=" + sec.cx + "&imgColorType=color&q=" + URLEncoder.encode(str, "UTF-8")
    println(">  getting " + url)

    val src = Try(get(url))
    println("> loaded")

    val res =
      if (src.isFailure) {
        println("> timeout or error: " + src.failed.get)
        s + "orry, cannot find an image in time for " + str
      } else {
        val imgSrc = parseGoogleJson(src.get)
        println("> parsed")
        if (imgSrc.isEmpty)
          s + "orry, search returned nothing for " + str
        else
          imgSrc.get + ""
      }

    println("> Res: " + res)
    reply(res, replyMarkup = Some(InlineKeyboardMarkup(List(
      List(n).map(x => InlineKeyboardButton(x, callbackData = Some(x))),
      List(w, e).map(x => InlineKeyboardButton(x, callbackData = Some(x))),
      List(s).map(x => InlineKeyboardButton(x, callbackData = Some(x)))
    ))))
  }

  onEditedMessage { implicit msg =>
    reply("You cannot change your past. Please answer with a direction.", replyMarkup = markup)
  }

  def process(text: String)(implicit msg: Message): String = {
    println(msg.chat.username.getOrElse("") + ": " + text)
    val current = 2658434
    val url = "http://api.geonames.org/neighboursJSON?geonameId=" + current + "&username=" + sec.geoUser
    Try(get(url)) match {
      case Failure(x) =>
        "Try /start again later. No knowledge about the world now. (Info: " + x + ")"
      case Success(x) =>
        val youAreHere: Option[String] =
          JSON.parseRaw(x) match {
            case Some(x: JSONObject) =>
              x.obj.find(_._1 == "geonames") match {
                case Some((_, x: JSONArray)) if x.list.nonEmpty =>
                  // TODO north of x is y ...
                  x.list(Math.floor(x.list.length * Math.random).toInt) match {
                    case x: JSONObject =>
                      x.obj.find(_._1 == "name") match {
                        case Some((_, x)) =>
                          Some(x + "")
                        case x => error(x)
                      }
                    case x => error(x)
                  }
                case x => error(x)
              }
            case x => error(x)
          }
        val here = "You are in " + youAreHere.getOrElse("an unknown country") + "."
        val res =
          if (text == start) {
            "Welcome to the world. Let me guide you through an adventure. You can answer North (n), South (s), West (w) and East (e), nothing else.\n" + here + "\nWhere do you want to go?"
          } else {
            val obj = "Goblin " + System.currentTimeMillis
            "You go " + text + ". " + here + "\n You find " + obj + ". You chop its head of.\nWhere to go next?"
          }
        println(res)
        res
    }
  }

  def markup: Option[ReplyMarkup] = {
    Some(ReplyKeyboardMarkup(List(
      List(n).map(x => KeyboardButton(x)),
      List(w, e).map(x => KeyboardButton(x)),
      List(s).map(x => KeyboardButton(x))
    )))
  }

  onMessage { implicit msg =>
    val origInput = msg.text.getOrElse("")
    val str = Map("n" -> n, "e" -> e, "w" -> w, "s" -> s).getOrElse(origInput, origInput)
    val valid = List(n, w, e, s).contains(str)

    if (str.contains("/")) {
      reply("", replyMarkup = markup)
    } else if (!valid) {
      reply("Please answer with a direction.", replyMarkup = markup)
    } else {
      /*val someMarkup = Some(InlineKeyboardMarkup(List(
        List("N").map(x => InlineKeyboardButton(x, callbackData = Some(x))),
        List("W", "O").map(x => InlineKeyboardButton(x, callbackData = Some(x))),
        List("S").map(x => InlineKeyboardButton(x, callbackData = Some(x)))
      )))*/
      val value = process(str)
      reply(value, replyMarkup = markup)
    }
  }

  onCommand(_ => true) { implicit msg =>
    if (msg.text.getOrElse("") == start) {
      reply(process(start), replyMarkup = markup)
    } else {
      reply("Please answer with a direction.", replyMarkup = markup)
    }
  }

  // override def allowedUpdates: Option[Seq[UpdateType]] = UpdateType.

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