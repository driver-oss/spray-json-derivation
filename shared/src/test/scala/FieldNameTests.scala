package spray.json

import org.scalatest._

trait SnakeCaseFormats { self: DerivedFormats =>
  override def extractFieldName(paramName: String) =
    FieldNaming.substituteCamel(paramName, '_')
}
trait KebabCaseFormats { self: DerivedFormats =>
  override def extractFieldName(paramName: String) =
    FieldNaming.substituteCamel(paramName, '-')
}

object FieldNaming {

  @inline final private def isLower(ch: Char): Boolean =
    ((ch & 0x20) != 0) || (ch == '_')

  @inline def substituteCamel(paramName: String, substitute: Char) = {
    val length = paramName.length
    val builder = new StringBuilder(length)
    var i = 0
    while (i < length) {
      val cur = paramName(i)
      if (isLower(cur) && i + 1 < length) {
        builder.append(cur)
        val next = paramName(i + 1)
        if (!isLower(next)) {
          builder.append(substitute)
          builder.append((next ^ 0x20).toChar)
        } else {
          builder.append(next)
        }
        i += 1
      } else {
        builder.append((cur ^ 0x20).toChar)
      }
      i += 1
    }
    builder.result()
  }

}

class FieldNameTests extends FlatSpec with FormatTests {

  case class A(camelCASE: String, `__a_aB__`: Int, `a-a_B`: Int)
  case class B(camelCaseA: A)

  trait All extends DefaultJsonProtocol with DerivedFormats {
    implicit val bFormat = jsonFormat[B]
  }

  {
    object Protocol extends All with SnakeCaseFormats
    import Protocol._
    "snake_case" should behave like checkRoundtrip(
      B(A("helloWorld", 0, 0)),
      """{"camel_case_a":{"camel_case":"helloWorld","__a_a_b__":0,"a-a_b":0}}"""
    )
  }

  {
    object Protocol extends All with KebabCaseFormats
    import Protocol._
    "kebab-case" should behave like checkRoundtrip(
      B(A("helloWorld", 0, 0)),
      """{"camel-case-a":{"camel-case":"helloWorld","__a_a-b__":0,"a-a_b":0}}"""
    )
  }

}
