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
    ((ch & 0x20) != 0) && (ch != '_')
  @inline final private def isUpper(ch: Char): Boolean =
    ((ch & 0x20) == 0) && (ch != '_')

  @inline final private def toLower(ch: Char): Char =
    if (ch == '_') '_' else (ch | 0x20).toChar

  @inline final def substituteCamel(paramName: String, substitute: Char) = {
    val length = paramName.length
    val builder = new StringBuilder(length)
    var i = 0
    while (i < length) {
      val cur = paramName(i)
      builder.append(toLower(cur))
      if (isLower(cur) && (i + 1 < length)) {
        val next = paramName(i + 1)
        if (isUpper(next)) {
          builder.append(substitute)
          builder.append(toLower(next))
          i += 1
        }
      }
      i += 1
    }
    builder.result()
  }

}
