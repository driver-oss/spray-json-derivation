package spray.json

import org.scalatest._

class OptionFieldTests
    extends FlatSpec
    with FormatTests {

  sealed trait Super
  case class Opt(x: Option[Int]) extends Super

  object HideNull extends DerivedJsonProtocol {
    override def printNull = false
    implicit val optFmt = jsonFormat[Opt]
  }

  object ShowNull extends DerivedJsonProtocol {
    override def printNull = true
    implicit val optFmt = jsonFormat[Opt]
  }


  "Option fields with some value" should behave like checkRoundtrip(
    Opt(Some(2)),
    """{"x":2}"""
  )(HideNull.optFmt)

  "Option fields with some value (show null)" should behave like checkRoundtrip(
    Opt(Some(2)),
    """{"x":2}"""
  )(ShowNull.optFmt)

  "Option fields with null value" should behave like checkRoundtrip(
    Opt(None),
    """{}"""
  )(HideNull.optFmt)

  "Option fields with null value (show null)" should behave like checkRoundtrip(
    Opt(None),
    """{"x":null}"""
  )(ShowNull.optFmt)

  "Option fields with undefined value" should "deserialize" in {
    import HideNull._
    assert("{}".parseJson.convertTo[Opt] == Opt(None))
  }

  "Option fields with undefined value (show null)" should "deserialize" in {
    import ShowNull._
    assert("{}".parseJson.convertTo[Opt] == Opt(None))
  }

   {
     import ShowNull._
     implicit val superFmt = jsonFormat[Super]
     "Option fields of ADTs" should behave like checkRoundtrip(
       Opt(Some(2)): Super,
       """{ "@type": "Opt", "x":2}"""
     )
   }

  sealed trait Enum
  case class Value(x: Int) extends Enum
  case class Wrapper(enum: Option[Enum])

  import ShowNull._
  implicit val enumFormat: RootJsonFormat[Enum] = jsonFormat[Enum]
  implicit val superFmt: RootJsonFormat[Wrapper] = jsonFormat[Wrapper]
  "Option fields of inner ADTs" should behave like checkRoundtrip(
    Wrapper(Some(Value(1))),
    """{"enum":{"@type":"Value", "x": 1}}"""
  )

}

