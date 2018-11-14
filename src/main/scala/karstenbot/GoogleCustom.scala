package karstenbot

import java.net.URLEncoder

import play.api.libs.json.{JsSuccess, Json}

import scala.util.Try


case class CustomSearchItemImageJson(src: String)

case class CustomSearchItemMapJson(cse_image: Option[Vector[CustomSearchItemImageJson]])

case class CustomSearchItemJson(pagemap: Option[CustomSearchItemMapJson])

case class CustomSearchJson(items: Vector[CustomSearchItemJson])

object CustomSearchJson {
  implicit val a = Json.reads[CustomSearchItemImageJson]
  implicit val b = Json.reads[CustomSearchItemMapJson]
  implicit val c = Json.reads[CustomSearchItemJson]
  implicit val d = Json.reads[CustomSearchJson]
}

class GoogleCustom(sec: SecretTrait) {

  def parseGoogleJson(src: String): Try[String] = {
    CustomSearchJson.d.reads(Json.parse(src)) match {
      case JsSuccess(CustomSearchJson(x), _) => Try(x.head.pagemap.get.cse_image.get.head.src)
      case x => HttpHelpers.error(x)
    }
  }

  def google(str: String): Try[String] = {
    val url = "https://www.googleapis.com/customsearch/v1?key=" + sec.googleKey + "&cx=" + sec.cx + "&imgColorType=color&q=" + URLEncoder.encode(str, "UTF-8")
    val src = HttpHelpers.get(url)
    // println(src)
    src.flatMap(src =>
      parseGoogleJson(src)
    )
  }
}