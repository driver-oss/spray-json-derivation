package xyz.driver.json

import spray.json._

import org.scalatest._

class ProductTypeFormats
    extends FlatSpec
    with FormatTests
    with DerivedFormats
    with DefaultJsonProtocol {

  case class A()
  case class B(x: Int, b: String, mp: Map[String, Int])
  case class C(b: B)
  case object D
  case class E(d: D.type)
  case class F(x: Int)

  "No-parameter product" should behave like checkRoundtrip(A(), "{}")

  "Simple parameter product" should behave like checkRoundtrip(
    B(42, "Hello World", Map("a" -> 1, "b" -> -1024)),
    """{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } }"""
  )

  "Nested parameter product" should behave like checkRoundtrip(
    C(B(42, "Hello World", Map("a" -> 1, "b" -> -1024))),
    """{"b" :{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } } }"""
  )

  "Case object" should behave like checkRoundtrip(
    D,
    "{}"
  )

  "Case object as parameter" should behave like checkRoundtrip(
    E(D),
    """{"d":{}}"""
  )

  // custom format for F, that inverts the value of parameter x
  implicit val fFormat: RootJsonFormat[F] = new RootJsonFormat[F] {
    override def write(f: F): JsValue = JsObject("x" -> JsNumber(-f.x))
    override def read(js: JsValue): F =
      F(-js.asJsObject.fields("x").convertTo[Int])
  }

  "Overriding with a custom format" should behave like checkRoundtrip(
    F(2),
    """{"x":-2}"""
  )

}
