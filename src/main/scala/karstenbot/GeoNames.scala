package karstenbot

import play.api.libs.json.{JsSuccess, Json, Reads}

import scala.util.{Failure, Success, Try}

case class GeoItem(name: String, geonameId: Long, lng: String, lat: String, countryCode: String)

case class GeoRes(geonames: Vector[GeoItem])

object GeoRes {
  implicit val a: Reads[GeoItem] = Json.reads[GeoItem]
  implicit val b: Reads[GeoRes] = Json.reads[GeoRes]
}

class GeoNames(sec: SecretTrait) {

  val earthRadius = 6371000.0

  private def calculate(x: Double): Double = {
    x
  }

  def deg2rad(deg: Double) = {
    deg * (Math.PI / 180)
  }

  // https://stackoverflow.com/a/27943/773842
  def distLngLat(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double = {
    val R = 6371 // Radius of the earth in km
    val dLat = deg2rad(lat2 - lat1) // deg2rad below
    val dLon = deg2rad(lon2 - lon1)
    val a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
          Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = R * c; // Distance in km
    d
  }

  def getNeighbor(current: Long): Try[(GeoItem, Vector[GeoItem])] = {
    getSelf(current) flatMap { cur =>
      getNeighbors(current) flatMap { neigh =>
        //var ls: List[(String, GeoItem)] = Nil
        //var item: GeoItem = null
        /*Direction.list.map(dir =>
          getCountryInDirs(cur, dir, neigh.filter(x => x.geonameId != current)) map { x =>
            ls = (dir, x) :: ls
          })*/
        Success(cur, neigh)
      }
    }
  }

  def getCountryInDirs(self: GeoItem, dir: String, neigh: Seq[GeoItem]): Try[GeoItem] = {
    val dist = 1000
    val tr = dir match {
      case Direction.n => Success(calculate(-dist) -> calculate(0))
      case Direction.s => Success(calculate(dist) -> calculate(0))
      case Direction.w => Success(calculate(0) -> calculate(-dist))
      case Direction.e => Success(calculate(0) -> calculate(dist))
      case x => HttpHelpers.error(x)
    }
    tr flatMap { case (dLat, dLng) =>
      val withDist = neigh.map { n =>
        n -> distLngLat(self.lng.toDouble + dLat, self.lat.toDouble + dLat, n.lng.toDouble, n.lat.toDouble)
      }
      val x = withDist.sortBy(_._2)
      println(self + " to " + x)
      Success(x.head._1)
    }
  }


  def getNeighbors(current: Long): Try[Vector[GeoItem]] = {
    val url = "http://api.geonames.org/neighboursJSON?geonameId=" + current + "&username=" + sec.geoUser
    HttpHelpers.get(url) match {
      case Failure(x) =>
        x.printStackTrace()
        Failure(x)
      case Success(x) =>
        // println(x)
        GeoRes.b.reads(Json.parse(x)) match {
          case JsSuccess(GeoRes(x), _) =>
            Success(x)
          case x => HttpHelpers.error(x)
        }
    }
  }

  def getSelf(current: Long): Try[GeoItem] = {
    val url = "http://api.geonames.org/getJSON?geonameId=" + current + "&username=" + sec.geoUser
    HttpHelpers.get(url) match {
      case Failure(x) =>
        Failure(x)
      case Success(x) =>
        // println(x)
        GeoRes.a.reads(Json.parse(x)) match {
          case JsSuccess(item, _) =>
            Success(item)
          case x => HttpHelpers.error(x)
        }
    }
  }
}