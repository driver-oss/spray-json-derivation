package spray.json

/** Serialize parametersOfCaseClasses as parameters_of_case_classes. */
trait SnakeCase { self: DerivedFormats =>
  override def extractFieldName(paramName: String) =
    FieldNaming.substituteCamel(paramName, '_')
}

/** Serialize parametersOfCaseClasses as parameters-of-case-classes. */
trait KebabCase { self: DerivedFormats =>
  override def extractFieldName(paramName: String) =
    FieldNaming.substituteCamel(paramName, '-')
}

object FieldNaming {

  @inline final private def isLower(ch: Char): Boolean =
    ((ch & 0x20) != 0) || (ch == '_')

  @inline final def substituteCamel(paramName: String, substitute: Char) = {
    val length = paramName.length
    val builder = new StringBuilder(length)
    var i = 0
    while (i < length) {
      val cur = paramName(i)
      val lower = isLower(cur)
      if (lower) {
        builder.append(cur)
      } else {
        builder.append((cur ^ 0x20).toChar)
      }
      if (lower && i + 1 < length) {
        val next = paramName(i + 1)
        if (!isLower(next)) {
          builder.append(substitute)
          builder.append((next ^ 0x20).toChar)
        } else {
          builder.append(next)
        }
        i += 1
      }
      i += 1
    }
    builder.result()
  }

}
