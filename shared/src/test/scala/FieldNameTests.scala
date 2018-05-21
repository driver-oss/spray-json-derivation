package spray.json

import org.scalatest._

class FieldNameTests extends FlatSpec with FormatTests {

  case class A(camelCASE: String, `__a_aB__`: Int, `a-a_B`: Int)
  case class B(camelCaseA: A)
  case class C(abA: String)

  trait All extends DefaultJsonProtocol with DerivedFormats {
    implicit val bFormat = jsonFormat[B]
    implicit val cFormat = jsonFormat[C]
  }

  {
    object Protocol extends All with SnakeCase
    import Protocol._
    "snake_case" should behave like checkRoundtrip(
      B(A("helloWorld", 0, 0)),
      """{"camel_case_a":{"camel_case":"helloWorld","__a_a_b__":0,"a-a_b":0}}"""
    )
    "abA" should "serialize correctly" in {
      assert(C("test").toJson === """{"ab_a":"test"}""".parseJson)
    }
  }

  {
    object Protocol extends All with KebabCase
    import Protocol._
    "kebab-case" should behave like checkRoundtrip(
      B(A("helloWorld", 0, 0)),
      """{"camel-case-a":{"camel-case":"helloWorld","__a_a-b__":0,"a-a_b":0}}"""
    )
  }

}
