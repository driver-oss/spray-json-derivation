package spray.json

import scala.annotation.StaticAnnotation

/** An annotation that designates that a sealed trait is an algebraic datatype
  * (ADT), and that a field containing the serialized childrens' types should be
  * added to the final JSON objects.
  *
  * Note that by default all sealed traits are treated as ADTs, with a type
  * field called `type`. This annotation enables overriding the name of that
  * field and is really only useful if a child itself has a field called `type`
  * that would otherwise result in a conflict.
  *
  * Example:
  * {{{
  * // the JSON field "kind" will contain the actual type of the serialized child
  * @adt("kind") sealed abstract class Keyword(`type`: String)
  * case class If(`type`: String) extends Keyword(`type`)
  * }}}
  * @param typeFieldName the name of the field to inject into a serialized JSON
  *        object */
final class adt(val typeFieldName: String = "type") extends StaticAnnotation
