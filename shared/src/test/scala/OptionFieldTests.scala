package spray.json

import org.scalatest._

class OptionFieldTests
    extends FlatSpec
    with FormatTests {

  case class Opt(x: Option[Int])

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

}

