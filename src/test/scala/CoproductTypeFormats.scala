package xyz.driver.json

import spray.json._

import org.scalatest._

class CoproductTypeFormats
    extends FlatSpec
    with FormatTests
    with DefaultJsonProtocol
    with DerivedFormats {

  sealed trait Expr
  case class Zero() extends Expr
  case class Value(x: Int) extends Expr
  case class Plus(lhs: Expr, rhs: Expr) extends Expr
  case object One extends Expr

  "No-parameter case class child" should behave like checkRoundtrip[Expr](
    Zero(),
    """{"type":"Zero"}"""
  )

  "Simple parameter case class child" should behave like checkRoundtrip[Expr](
    Value(42),
    """{"type":"Value","x":42}"""
  )

  "Nested parameter case class child" should behave like checkRoundtrip[Expr](
    Plus(Value(42), One),
    """{"type":"Plus","lhs":{"type":"Value","x":42},"rhs":{"type":"One"}}"""
  )

  "Case object child" should behave like checkRoundtrip[Expr](
    One,
    """{"type": "One"}"""
  )

  @gadt("kind")
  sealed abstract class Keyword(`type`: String)
  case class If(`type`: String) extends Keyword(`type`)

  "GADT with type field alias" should behave like checkRoundtrip[Keyword](
    If("class"),
    """{"kind":"If","type":"class"}"""
  )

  @gadt("""_`crazy type!`"""")
  sealed abstract trait Crazy
  case class CrazyType() extends Crazy

  "GADT with special characters in type field" should behave like checkRoundtrip[
    Crazy](
    CrazyType(),
    """{"_`crazy type!`\"": "CrazyType"}"""
  )

  sealed trait Enum
  case object A extends Enum
  case object B extends Enum

  "Enum" should behave like checkRoundtrip[List[Enum]](
    A :: B :: Nil,
    """[{"type":"A"}, {"type":"B"}]"""
  )

  "Serializing as sealed trait an deserializing as child" should "work" in {
    val expr: Expr = Plus(Value(42), Plus(Zero(), One))
    assert(expr.toJson.convertTo[Plus] == expr)
  }

}
