package karstenbot

import java.net.URL
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.models._
import scalaj.http.{Http, HttpOptions}

import scala.util.{Failure, Random, Success, Try}

class KarstenBot(val sec: SecretTrait) extends TelegramBot
  with Polling
  with Commands {
  val client = new ScalajHttpClient(sec.telegramToken)
  val rng = new Random(System.currentTimeMillis)

  val start: String = "/start"

  val db = new Database(sec)
  if (!db.test) {
    sys.error("DB not accessible: " + db.url)
  }

  /*
  def actionOn(implicit msg: Message): Future[Message] = {
    val str = msg.text.getOrElse("")
    println("Got message from " + msg.chat.username.getOrElse("") + " (" + msg.chat.firstName.getOrElse("") + "): " + str)

    val imgScr = new GoogleCustom(sec).google(str)

    val res =
      if (imgScr.isFailure) {
        println("> error: " + imgScr.failed.get)
        "Sorry, cannot find an image in time for " + str
      } else {
        imgScr.get
      }

    println("> Res: " + res)
    reply(res, replyMarkup = markup(Nil))
  }

  def markup2: Option[InlineKeyboardMarkup] = {
    Some(InlineKeyboardMarkup(List(
      List(Direction.n).map(x => InlineKeyboardButton(x, callbackData = Some(x))),
      List(Direction.w, Direction.e).map(x => InlineKeyboardButton(x, callbackData = Some(x))),
      List(Direction.s).map(x => InlineKeyboardButton(x, callbackData = Some(x)))
    )))
  }

  def markup3(lst: List[String]): Option[ReplyKeyboardMarkup] = {
    Some(ReplyKeyboardMarkup(List(
      List(Direction.n).map(x => KeyboardButton(x)),
      List(Direction.w, Direction.e).map(x => KeyboardButton(x)),
      List(Direction.s).map(x => KeyboardButton(x))
    )))
  }*/

  def markup(lst: Seq[String]): Option[ReplyKeyboardMarkup] = {
    Some(ReplyKeyboardMarkup(lst.map(x => KeyboardButton(x)).toList.grouped(3).toList))
  }

  onEditedMessage { implicit msg =>
    reply("You cannot change your past. Please answer with a button.", replyMarkup = markup(Nil))
  }


  onMessage { implicit msg =>
    val str = msg.text.getOrElse("")
    if (str == start) {
      reply("")
    } else {
      val arr = CardLoader.generateCardImage(str)
      //reply()
      println("converting")
      //val url = "https://api.telegram.org/bot" + sec.telegramToken + "/sendPhoto";
      val base64 = Base64.getEncoder.encodeToString(arr)
      println("done")
      /*val jspon: String = """{"chat_id:":""" + msg.chat.id + """, "photo": """" + base64 + """"}}"""

      println(url)
      println(jspon)
      val result =
        Http(url)
          .postData(jspon)
          .header("Content-Type", "application/json")
          .header("Charset", "UTF-8")
          .option(HttpOptions.readTimeout(10000)).asString

      println(result)*/

      // "<img src='data:image/png;base64," + base64 + "'>"
      reply("Done ")
    }
  }

  onCommand(_ => true) { implicit msg =>
    if (msg.text.getOrElse("") == start) {
      reply("Welcome. type a name and see a generated card with that name")
    } else {
      reply("")
    }
  }

  /*
  onMessage { implicit msg =>
    val str = msg.text.getOrElse("")
    if (str == start) {
      reply("", replyMarkup = markup(Nil))
    } else {
      val (res, items) = process(str)
      reply(res, replyMarkup = markup(items))
    }
  }

  onCommand(_ => true) { implicit msg =>
    if (msg.text.getOrElse("") == start) {
      val (item, x) = process(start)
      reply(item, replyMarkup = markup(x))
    } else {
      reply("Please answer with a button.", replyMarkup = markup(Nil))
    }
  }*/

  val locations = new ConcurrentHashMap[String, Long]()

  def process(text: String)(implicit msg: Message): (String, Seq[String]) = {
    val str = "INSERT INTO `teleLog`(`firstName`, `userId`, `message`, `timestamp`) VALUES (?, ?, ?, ?);"
    val stm = db.connect.prepareStatement(str)
    stm.setString(1, msg.chat.firstName.orNull)
    stm.setString(2, msg.chat.username.orNull)
    stm.setString(3, text)
    stm.setLong(4, System.currentTimeMillis)
    stm.executeUpdate

    val user = msg.chat.username.getOrElse("")
    val old =
      if (locations.contains(user)) {
        locations.get(user)
      } else {
        val germany = 2921044
        germany
      }
    println(user + ": " + text)

    val geo = new GeoNames(sec)

    val id = if (text == start) {
      Success(old)
    } else {
      Try(text.split("#").lastOption.map(_.toLong).get)
    }
    // println("id " + id)
    id match {
      case Failure(x) =>
        x.printStackTrace()
        error(x)
      case Success(id) =>
        geo.getNeighbor(old) match {
          case Failure(x) =>
            x.printStackTrace()
            error(x)
          case Success((self1, neighbors1)) =>
            val triedTuple =
              if (old == id) Success((self1, neighbors1)) else geo.getNeighbor(id)
            triedTuple match {
              case Failure(x) =>
                x.printStackTrace()
                error(x)
              case Success((self2, neighbors2)) =>
                locations.put(user, self2.geonameId)
                val here =
                /*if (old != id && !neighbors1.exists(_.geonameId == self2.geonameId)) {
                  "Selected country is not adjacent. Select an adjacent country."
                } else TODO */
                  if (text == start) {
                    "Welcome to the world. Let me guide you through an adventure. You start in Germany.\n(Be a world traveller: You can access US from Spain and Europe from US.)\nSelect a country from below."
                  } else {
                    val img = Failure(null) // new GoogleCustom(sec).google(self2.name + " Wikipedia -flag")
                    "You are in " + self2.name + ". " + img.toOption.getOrElse("") + "\n"
                  }

                val spain = GeoItem("Fly to Spain", 2510769, "0", "0", "")
                val us = GeoItem("Fly to United States", 6252001, "0", "0", "")

                val neighbors = neighbors2 ++
                  (if (id == spain.geonameId) {
                    List(us)
                  } else if (id == us.geonameId)
                    List(spain)
                  else Nil)
                val res =
                  here + "\nThese are adjacent: " + neighbors.map(x => x.name).mkString(", ") + "\nWhere to go next?"
                println("> " + res)
                (res, neighbors.map(x => x.name + "#" + x.geonameId.toString))
            }
        }
    }
  }

  def error(x: Throwable): (String, List[String]) = {
    ("Try /start again later. No knowledge about the world now.\nTechnical error: " + x, List(start))
  }
}
