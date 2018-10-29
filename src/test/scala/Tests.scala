
import karstenbot.{Direction, GeoNames, GoogleCustom, SecretTrait}
import org.specs2._
import org.specs2.matcher.Matchers

class Tests extends Specification with Matchers {
  val Germany = 'Germany -> 2921044
  val Austria = 'Austria -> 2782113
  val France = 'France -> 3017382
  val Italy = 'Italy -> 3175395

  def is =
    s2"""
    Setup ok: $ok
    Starts with: ${
      "str" should startingWith("s")
    }
   Fail: ${
      "t" should startingWith("s")
    }
   Google: ${
      val res = new GoogleCustom(SecretTrait.impl).google("matrix movie")
      res shouldNotEqual None
      res.get should startWith("http")
    }
  Geo:
      ${checkNeighborInDirection(Germany, Direction.s, Austria)/*}
      ${checkNeighborInDirection(Austria, Direction.s, Italy)}

      ${checkNeighborInDirection(Austria, Direction.n, Germany)}

      ${checkNeighborInDirection(Germany, Direction.w, France)}
      ${checkNeighborInDirection(Germany, Direction.w, France)*/}
  """

  private def checkNeighborInDirection(country1: (Symbol, Int), dir: String, neighbor: (Symbol, Int)) = {
    val res = new GeoNames(SecretTrait.impl).getNeighbor(dir, country1._2)
    res shouldNotEqual None
    val resBool = res.get._1.geonameId == neighbor._2
    val str = s"$dir of ${country1._1} is ${neighbor._1} (actual: ${res.get._1.name} / ${res.get._1.geonameId})"
    println(str + " => " + resBool)
    res.get._1.geonameId aka str shouldEqual neighbor._2
  }
}
