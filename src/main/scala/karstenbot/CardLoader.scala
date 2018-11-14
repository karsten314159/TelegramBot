package karstenbot

import java.io._
import java.nio.file._

import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{SVGAbstractTranscoder, TranscoderInput, TranscoderOutput}
import org.jsoup.Jsoup
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Failure
import scala.util.matching.Regex

object CardLoader {

  val wikiApi = "https://en.wikipedia.org/w/api.php?action=parse&format=json&page="

  def killers: List[CardData] = {
    val url = wikiApi + "List_of_serial_killers_by_number_of_victims"
    val jsonString = Source.fromURL(url).mkString
    WikiRes.c.reads(Json.parse(jsonString)) match {
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
                Name = item.head,
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
            Description1 = c.Notes.substring(0, c.Notes.length.min(20)).trim,
            Description2 = ""
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
    val jsonString = Source.fromURL(url).mkString
    WikiRes.c.reads(Json.parse(jsonString)) match {
      case JsSuccess(x, _) =>
        println(x)
        val doc = Jsoup.parse(x.parse.text.`*`)
        val cards = ListBuffer[CardData]()
        doc.getElementsByTag("li").spliterator.forEachRemaining { li =>
          // TODO: remove
          if (true) {
            val parts = li.text.split(",")
            if (parts.length > 2) {
              val nameWithYear = parts(0)
              val idx = nameWithYear.indexOf(" (")
              val nameWithoutYear = nameWithYear.substring(0, if (idx == -1) nameWithYear.length - 1 else idx)
              val triedString = Failure(null) // new GoogleCustom(SecretTrait.impl).google(nameWithoutYear)
              val img = triedString.toOption
              //if (img.isDefined) {
              val imgStdUrl = "https://pharm.ucsf.edu/sites/pharm.ucsf.edu/files/styles/lab_half/public/RS16062_MSS2011_23_photo_prints_classrooms_labs_14_095%20sepia%201x1.jpg?itok=X4tFpPEE"
              val img2 = "https://assets-cdn.github.com/images/modules/logos_page/Octocat.png"
              val descText = parts.drop(1).mkString(" ")
              val (desc1, desc2) = format(descText)
              cards += CardData(
                nameWithoutYear,
                "Chemist",
                descText.length + "",
                nameWithoutYear.length + "",
                "",
                "Wikipedia",
                img.getOrElse(imgStdUrl),
                desc1, desc2
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

  private def format(descText: String): (String, String) = {
    val strings2 = descText.split(" ")
    val strings = strings2.grouped(7).map(x => x.mkString(" ")).toList
    strings.head -> strings.drop(1).headOption.map(x => x + "...").getOrElse("")
  }

  // http://www.svgopen.org/2002/papers/kormann__developing_svg_apps_with_batik
  def renderSvgToBmp(con: String): Array[Byte] = {
    // Create a PNG transcoder
    val transcoder = new PNGTranscoder

    val fac = 1f
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, fac * 408f)
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, fac * 569f)

    // Create the transcoder input
    val input = new TranscoderInput(new StringReader(con))
    // Create the transcoder output
    val ostream = new ByteArrayOutputStream
    val output = new TranscoderOutput(ostream)

    // Transform the svg document into a PNG image
    transcoder.transcode(input, output)

    // Flush and close the stream
    ostream.flush()
    ostream.close()
    ostream.toByteArray
  }

  //https:// https://en.wikipedia.org/wiki/Lists_of_mathematicians https://en.wikipedia.org/wiki/List_of_chemists https://en.wikipedia.org/wiki/List_of_physicists
  def main2(args: Array[String]): Unit = {
    generateCardImage("Name")
  }

  def generateCardImage(name: String): (Array[Byte], Path) = {
    //val items = chemists.take(3)
    val folder = new File("").getAbsolutePath
    val cardSvg = folder + "/card.svg"
    val res = folder + "/output/"
    println(cardSvg)

    val svgTemplate = Source.fromFile(cardSvg).mkString

    val items = List(name)
    //println(new String(items.map(_ => '_').toArray))
    println("generating image")
    val len = items.length
    var arr = Array(0: Byte)
    val (namedItem, i) = items.zipWithIndex.head
    val seed = namedItem.map(_.toInt).sum //fold(0)((a, b) => a + b)
    val gen = CardGen.generate("c_" + seed)
    val c = gen.copy(CardName = namedItem)
    print(".")
    val con = svgTemplate
      .replace("$CardName", c.CardName) // Auf "&" in der Entityreferenz muss umgehend der Entityname folgen.
      .replace("$CardType", c.CardType)
      .replace("$Field", c.Field)
      .replace("$Costs", c.Costs)
      .replace("$Stamp", c.Stamp)
      .replace("$ArtistName", c.ArtistName)
      .replace("$ImageHref", c.ImageHref)
      .replace("$Description1", c.Description1)
      .replace("$Description2", c.Description2)
      .replace("$Number", i + "/" + len)

    arr = renderSvgToBmp(con)
    println("done")

    val ext = "png"
    val baseFile = res + c.CardName.toLowerCase.replaceAll("[^a-z ]", "")
    //"svg"
    val path: Path = Paths.get(baseFile + "." + ext)
    Files.write(path, arr, StandardOpenOption.CREATE)
    /*
    val path2: Path = Paths.get(baseFile + "." + "svg")
    val b = new BufferedWriter(new FileWriter(path2.toFile))
    b.write(con, 0, con.length)
    b.close()
    */

    /*println("")
    println("Done with " + items.length + " items")*/
    arr -> path
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
                     Description1: String,
                     Description2: String
                   )