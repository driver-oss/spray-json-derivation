package xyz.driver.json

import spray.json._

import org.scalatest._

class ProductTypeFormatTests
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
  case class G(f: F)

  implicit val aFormat: RootJsonFormat[A] = jsonFormat[A]
  implicit val bFormat: RootJsonFormat[B] = jsonFormat[B]
  implicit val cFormat: RootJsonFormat[C] = jsonFormat[C]
  implicit val dFormat: RootJsonFormat[D.type] = jsonFormat[D.type]
  implicit val eFormat: RootJsonFormat[E] = jsonFormat[E]

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
    override def write(f: F): JsValue = JsObject("y" -> f.x.toJson)
    override def read(js: JsValue): F =
      F(js.asJsObject.fields("y").convertTo[Int])
  }

  "Overriding with a custom format" should behave like checkRoundtrip(
    F(2),
    """{"y":2}"""
  )

  implicit val gFormat: RootJsonFormat[G] = jsonFormat[G]

  "Derving a format with a custom child format" should behave like checkRoundtrip(
    G(F(2)),
    """{"f": {"y":2}}"""
  )

  case class H(x: Boolean)
  case class I(h: H)

  // there is no format defined for H, Magnolia will generate one automatically
  implicit val iFormat: RootJsonFormat[I] = jsonFormat[I]

  "Deriving a format that has no implicit child formats available" should behave like checkRoundtrip(
    I(H(true)),
    """{"h": {"x":true}}"""
  )

}
