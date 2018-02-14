package xyz.driver.json

import scala.annotation.StaticAnnotation

/** Indicator trait of anontations related to JSON formatting. */
sealed trait JsonAnnotation

/** An annotation that designates that a sealed trait is a generalized algebraic
  * datatype (GADT), and that a type field containing the serialized childrens'
  * types should be added to the final JSON objects.
  *
  * Note that by default all sealed traits are treated as GADTs, with a type
  * field called `type`. This annotation enables overriding the name of that
  * field and is really only useful if a child itself has a field called `type`
  * that would result in a conflict.
  *
  * Example
  * ```
  * // the JSON field "kind" will contain the actual type of the serialized child
  * @gadt("kind") sealed abstract class Keyword(`type`: String)
  * case class If(`type`: String) extends Keyword(`type`)
  * ```
  *
  * @param typeFieldName the name of the field to inject into a serialized JSON
  *        object */
final class gadt(val typeFieldName: String = "type")
    extends StaticAnnotation
    with JsonAnnotation

/** An annotation that designates that a sealed trait is an enumeration (all
  * children are strictly case objects), and that all children should be
  * serialized as strings. */
final class enum extends StaticAnnotation with JsonAnnotation
