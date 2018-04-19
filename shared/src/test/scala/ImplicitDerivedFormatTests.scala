package spray.json

import org.scalatest._

class ImplicitDerivedFormatTests
    extends FlatSpec
    with FormatTests
    with ImplicitDerivedJsonProtocol {

  case class B(x: Int, b: String, mp: Map[String, Int])
  case class C(b: B)

  "Simple parameter product" should behave like checkRoundtrip(
    B(42, "Hello World", Map("a" -> 1, "b" -> -1024)),
    """{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } }"""
  )

  "Nested parameter product with custom child format" should behave like checkRoundtrip(
    C(B(42, "Hello World", Map("a" -> 1, "b" -> -1024))),
    """{"b" :{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } } }"""
  )

}
