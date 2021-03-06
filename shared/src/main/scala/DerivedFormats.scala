package spray.json

import magnolia._

import scala.language.experimental.macros

/** Mixin that enables derivation of JSON formats for any product
  * (case classes) or coproduct (sealed traits) types. */
trait DerivedFormats { self: BasicFormats =>
  type Typeclass[T] = JsonFormat[T]

  /** Convert the name of a parameter to that of a field in a JSON object. This
    * method can be overriden to use alternative naming conventions. */
  @inline def extractFieldName(paramName: String): String = paramName

  /** Determines if `None` instances of options should be included in JSON output.
    *
    * By default, `None` values are ommitted entirely from resulting JSON
    * objects. If overridden, they will be included as `null`s instead.
    *
    * Note that this has no effect in *reading* option types; undefined or null
    * values are always converted to `None`. */
  def printNull: Boolean = false

  def combine[T](ctx: CaseClass[JsonFormat, T]): JsonFormat[T] =
    new JsonFormat[T] {
      override def write(value: T): JsValue =
        if (ctx.isValueClass) {
          val param = ctx.parameters.head
          param.typeclass.write(param.dereference(value))
        } else {
          val fields: Seq[(String, JsValue)] = ctx.parameters.map { param =>
            extractFieldName(param.label) -> param.typeclass.write(
              param.dereference(value)
            )
          }
          val nullFiltered = fields.filter {
            case (_, JsNull) => printNull
            case (_, _)      => true
          }
          JsObject(nullFiltered: _*)
        }

      override def read(value: JsValue): T = value match {
        case js if ctx.isValueClass =>
          ctx.construct(param => param.typeclass.read(value))
        case obj: JsObject =>
          if (ctx.isObject) {
            ctx.rawConstruct(Seq.empty)
          } else {
            ctx.construct { param =>
              val fieldName = extractFieldName(param.label)
              val fieldValue = obj.fields.getOrElse(fieldName, JsNull)
              param.typeclass.read(fieldValue)
            }
          }
        case js =>
          deserializationError(
            s"Cannot read JSON '$js' as a ${ctx.typeName.full}"
          )
      }
    }

  def dispatch[T](ctx: SealedTrait[JsonFormat, T]): JsonFormat[T] = {
    val typeFieldName = ctx.annotations
      .collectFirst {
        case g: adt => g.typeFieldName
      }
      .getOrElse("@type")

    new JsonFormat[T] {
      override def write(value: T): JsValue = ctx.dispatch(value) { sub =>
        sub.typeclass.write(sub.cast(value)) match {
          case obj: JsObject =>
            JsObject(
              (Map(typeFieldName -> JsString(sub.typeName.short)) ++
                obj.fields).toSeq: _*
            )
          case js => js
        }
      }

      override def read(js: JsValue): T = {
        val typeName: String = js match {
          case obj: JsObject if obj.fields.contains(typeFieldName) =>
            obj.fields(typeFieldName).convertTo[String]
          case JsString(str) =>
            str
          case _ =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName.full} " +
                "because serialized type cannot be determined."
            )
        }

        ctx.subtypes.find(_.typeName.short == typeName) match {
          case Some(tpe) => tpe.typeclass.read(js)
          case None =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName.full} " +
                s"because type '${typeName}' is unsupported."
            )
        }
      }
    }
  }

  def jsonFormat[T]: RootJsonFormat[T] =
    macro DerivedFormatMacros.derivedFormat[T]

}

@deprecated("use DerivedJsonProtocol", "spray-json-derivation 0.4.3")
object DerivedFormats extends DerivedFormats with DefaultJsonProtocol

trait ImplicitDerivedFormats extends DerivedFormats { self: BasicFormats =>
  implicit def implicitJsonFormat[T]: RootJsonFormat[T] =
    macro DerivedFormatMacros.derivedFormat[T]
}

@deprecated("use ImplicitDerivedJsonProtocol", "spray-json-derivation 0.4.3")
object ImplicitDerivedFormats
    extends ImplicitDerivedFormats
    with DefaultJsonProtocol

trait DerivedJsonProtocol extends DefaultJsonProtocol with DerivedFormats
object DerivedJsonProtocol extends DerivedJsonProtocol

trait ImplicitDerivedJsonProtocol
    extends DefaultJsonProtocol
    with ImplicitDerivedFormats
object ImplicitDerivedJsonProtocol extends ImplicitDerivedJsonProtocol

object DerivedFormatMacros {
  import scala.reflect.macros.whitebox._

  /** Utility that converts a magnolia-generated JsonFormat to a RootJsonFormat. */
  def derivedFormat[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val sprayPkg = c.mirror.staticPackage("spray.json")
    val valName = TermName(c.freshName("format"))
    q"""{
      val $valName = ${Magnolia.gen[T](c)}
      new $sprayPkg.RootJsonFormat[$tpe] {
        def write(value: $tpe) = $valName.write(value)
        def read(value: $sprayPkg.JsValue) = $valName.read(value)
      }
    }"""
  }
}
