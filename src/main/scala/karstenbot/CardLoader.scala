package karstenbot

import java.io.{BufferedWriter, FileOutputStream, FileWriter}
import java.nio.file._

import org.jsoup.Jsoup
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex

object CardLoader {
  val url = "https://en.wikipedia.org/w/api.php?action=parse&page=List_of_serial_killers_by_number_of_victims&format=json"

  def main2(args: Array[String]): Unit = {
    val x = Source.fromURL(url).mkString
    WikiRes.c.reads(Json.parse(x)) match {
      case JsSuccess(x, _) =>
        //println(x)
        val doc = Jsoup.parse(x.parse.text.`*`)
        ////println(doc)
        val trs = doc.body.getElementsByTag("tr")

        val cards = ListBuffer[Card]()
        trs.spliterator.forEachRemaining { x =>
          val item = ListBuffer[String]()
          val tds = x.getElementsByTag("td")
          tds.spliterator.forEachRemaining { td =>
            item += td.text
          }
          val reg = new Regex(""""([^"]*)*"""")
          item.find(w => reg.findFirstIn(w).isDefined) match {
            case Some(note) =>
              val n = note.split("\"")(1).replace("murders", "Killer").replace("Murders", "Killer")
              //reg.findAllIn(note).group(1)
              val card = Card(
                Nickname = n,
                Name = item(0),
                Country = item(1),
                YearsActive = item(2),
                ProvenVictims = item(3),
                PossibleVictims = item(4),
                Notes = item(5)
              )
              cards += card

            case _ => ()
          }

        }
        val folder = "C:/Projekte/Scala/TelegramBot/"
        val cardSvg = folder + "/card.svg"
        val res = folder + "/output/"

        val svgTemplate = Source.fromFile(cardSvg).mkString
        for (c <- cards) {
          val con = svgTemplate
            .replace("Trading Card Monster Name", c.Nickname)
            .replace("Monster Type", c.Name)
            .replace("A5", c.ProvenVictims)
            .replace("Designer/Artist Name", c.Country)
          val path: Path = Paths.get(res + c.Name.toLowerCase.replaceAll("[^a-z ]", "") + ".svg")
          /*val strings: Iterable[_ <: CharSequence] = List(con)
          val utf: Charset = StandardCharsets.UTF_8
          val create: OpenOption = StandardOpenOption.CREATE
          Files.write(
            path,
            strings,
            utf,
            create
          )*/
          val b = new BufferedWriter(new FileWriter(path.toFile))
          b.write(con, 0, con.length)
          b.close()
        }
      case JsError(e) =>
        println(e)
    }
  }
}

case class Wiki2(`*`: String)

case class Wiki1(title: String, text: Wiki2)

case class WikiRes(parse: Wiki1)

object WikiRes {
  implicit val a: Reads[Wiki2] = Json.reads[Wiki2]
  implicit val b: Reads[Wiki1] = Json.reads[Wiki1]
  val c: Reads[WikiRes] = Json.reads[WikiRes]
}

case class Card(
                 Nickname: String,
                 Name: String,
                 Country: String,
                 YearsActive: String,
                 ProvenVictims: String,
                 PossibleVictims: String,
                 Notes: String
               )