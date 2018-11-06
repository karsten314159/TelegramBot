package karstenbot

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file._

import org.jsoup.Jsoup
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Try
import scala.util.matching.Regex

object CardLoader {

  val wikiApi = "https://en.wikipedia.org/w/api.php?action=parse&format=json&page="

  def killers: List[CardData] = {
    val url = wikiApi + "List_of_serial_killers_by_number_of_victims"
    val x = Source.fromURL(url).mkString
    WikiRes.c.reads(Json.parse(x)) match {
      case JsSuccess(x, _) =>
        //println(x)
        val doc = Jsoup.parse(x.parse.text.`*`)
        ////println(doc)
        val trs = doc.body.getElementsByTag("tr")

        val cards = ListBuffer[Killer]()
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
              val card = Killer(
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

        println(new String(cards.map(_ => '_').toArray))
        cards.map { c =>
          val img = new GoogleCustom(SecretTrait.impl).google(c.Name).toOption
          val con = CardData(
            CardName = c.Nickname.trim,
            CardType = c.Name.trim,
            Field = c.PossibleVictims.trim,
            Costs = c.ProvenVictims.trim,
            Stamp = c.YearsActive.trim,
            ArtistName = c.Country.trim,
            ImageHref = img.getOrElse("https://cdn.cnn.com/cnnnext/dam/assets/180306160531-unmasking-a-killer-2-00000000-large-169.jpg"),
            Description = c.Notes.substring(0, c.Notes.length.min(20)).trim
          )
          con
        }.toList
      case e =>
        HttpHelpers.error(e)
        Nil
    }
  }

  def chemists: List[CardData] = {
    val url = wikiApi + "List_of_chemists"
    val x = Source.fromURL(url).mkString
    WikiRes.c.reads(Json.parse(x)) match {
      case JsSuccess(x, _) =>
        println(x)
        val doc = Jsoup.parse(x.parse.text.`*`)
        val cards = ListBuffer[CardData]()
        doc.getElementsByTag("li").spliterator.forEachRemaining { li =>
          // TODO: remove
          if (cards.length < 10) {
            val parts = li.text.split(",")
            if (parts.length > 2) {
              val nameWithYear = parts(0)
              val idx = nameWithYear.indexOf(" (")
              val nameWithoutYear = nameWithYear.substring(0, if (idx == -1) nameWithYear.length - 1 else idx)
              val triedString = new GoogleCustom(SecretTrait.impl).google(nameWithoutYear)
              val img = triedString.toOption
              //if (img.isDefined) {
              cards += CardData(
                nameWithoutYear,
                "Chemist",
                "",
                Try(parts(2)).toOption.getOrElse(""),
                Try(parts(3)).toOption.getOrElse(""),
                "",
                img.getOrElse("https://pharm.ucsf.edu/sites/pharm.ucsf.edu/files/styles/lab_half/public/RS16062_MSS2011_23_photo_prints_classrooms_labs_14_095%20sepia%201x1.jpg?itok=X4tFpPEE"),
                Try(parts(1)).toOption.getOrElse("")
              )
              //}
            }
          }
        }
        cards.toList

      case e =>
        HttpHelpers.error(e)
        Nil
    }
  }

  //https:// https://en.wikipedia.org/wiki/Lists_of_mathematicians https://en.wikipedia.org/wiki/List_of_chemists https://en.wikipedia.org/wiki/List_of_physicists
  def main2(args: Array[String]): Unit = {
    val items = chemists

    val cwd = new File("").getAbsolutePath
    val folder = cwd
    val cardSvg = folder + "/card.svg"
    val res = folder + "/output/"
    println(cardSvg)

    val svgTemplate = Source.fromFile(cardSvg).mkString

    println(new String(items.map(_ => '_').toArray))
    for (c <- items) {
      print(".")
      val con = svgTemplate
        .replace("$CardName", c.CardName)
        .replace("$CardType", c.CardType)
        .replace("$Field", c.Field)
        .replace("$Costs", c.Costs)
        .replace("$Stamp", c.Stamp)
        .replace("$ArtistName", c.ArtistName)
        .replace("$ImageHref", c.ImageHref)
        .replace("$Description", c.Description)
      val path: Path = Paths.get(res + c.CardName.toLowerCase.replaceAll("[^a-z ]", "") + ".svg")
      val b = new BufferedWriter(new FileWriter(path.toFile))
      b.write(con, 0, con.length)
      b.close()
    }
    println("")
    println("Done with " + items.length + " items")
  }
}

case class Killer(
                   Nickname: String,
                   Name: String,
                   Country: String,
                   YearsActive: String,
                   ProvenVictims: String,
                   PossibleVictims: String,
                   Notes: String
                 )


case class Wiki2(`*`: String)

case class Wiki1(title: String, text: Wiki2)

case class WikiRes(parse: Wiki1)

object WikiRes {
  implicit val a: Reads[Wiki2] = Json.reads[Wiki2]
  implicit val b: Reads[Wiki1] = Json.reads[Wiki1]
  val c: Reads[WikiRes] = Json.reads[WikiRes]
}


case class CardData(
                     CardName: String,
                     CardType: String,
                     Field: String,
                     Costs: String,
                     Stamp: String,
                     ArtistName: String,
                     ImageHref: String,
                     Description: String
                   )