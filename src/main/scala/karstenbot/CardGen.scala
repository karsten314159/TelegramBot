package karstenbot

import java.io._

import javax.script.{ScriptContext, ScriptEngineManager, SimpleScriptContext}
import org.jsoup.Jsoup

import scala.io.Source

object CardGen {
  def main2(args: Array[String]): Unit = {
    generate("uc_1231")
  }

  def generate(seed: String): CardData = {
    val js = "cardgen.js"

    val folder = new File("").getAbsolutePath
    val jsFile = folder + "/" + js

    val script = Source.fromFile(jsFile).mkString

    val manager = new ScriptEngineManager
    val engine = manager.getEngineByName("javascript")

    val newCtx = new SimpleScriptContext
    newCtx.setBindings(engine.createBindings, ScriptContext.ENGINE_SCOPE)
    val res =
      engine.eval(
        script.replace("/*value-here*/", "\"" + seed + "\""),
        newCtx)

    val str = res.toString
      .replaceAll("Image>", "ImageUrl>")
      .replaceAll("<NewLine />", "\n")
    val body = Jsoup.parse(str).body
    /*  val reader = XmlFactory.createParserFactory(true).newSAXParser.getXMLReader
      val res = reader.parse(new InputSource(new StringReader(res.toString)))
    */

    //val imageFormat = "http://magiccards.info/crop/en/%s.jpg"
    val imageFormat = "https://img.scryfall.com/cards/png/en/%s.png"

    val text = body.select("SubType").text
    val pow = body.select("Power").text
    val field =
      if (pow != "")
        pow + "/" + body.select("Toughness").text
      else
        body.select("Loyalty").text
    val (text1, text2) = format(body.select("Text").text)
    val costs = body.select("Cost").html.toUpperCase
      .replaceAll("MANA", "")
      .replaceAll("[</>\n ]", "").toSet.toList.sorted.mkString("")

    val cardType = body.select("Type").text + (if (text != "") " - " + text else "")

    val c =
      CardData(
        body.select("Name").text,
        cardType,
        field,
        costs,
        "",
        seed,
        imageFormat.format(body.select("ImageUrl").text),
        text1,
        text2
      )
    c
  }

  def format(descText: String): (String, String) = {
    val strings2 = descText.split("\n")
    strings2.head -> strings2.drop(1).headOption.getOrElse("")
  }
}

/*
<Cards>
    <Card>
        <Identity>u</Identity>
        <Name>Silent Grounds</Name>
        <Cost>
            <U />
        </Cost>
        <Image>jou/23</Image>
        <Type>Legendary Creature</Type>
        <SubType>Orc</SubType>
        <Text>Graft 4</Text>
        <Power>8</Power>
        <Toughness>5</Toughness>
        <Seed>uc_1231</Seed>
    </Card>
</Cards>
 */