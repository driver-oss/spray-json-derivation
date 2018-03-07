package xyz.driver.json

import magnolia._
import spray.json._

import scala.language.experimental.macros

/** Mixin that enables automatic derivation of JSON formats for any product
  * (case classes) or coproduct (sealed traits) types. */
trait DerivedFormats { self: BasicFormats =>
  type Typeclass[T] = JsonFormat[T]

  def combine[T](ctx: CaseClass[JsonFormat, T]): JsonFormat[T] =
    new JsonFormat[T] {
      override def write(value: T): JsValue = {
        val fields: Seq[(String, JsValue)] = ctx.parameters.map { param =>
          param.label -> param.typeclass.write(param.dereference(value))
        }
        JsObject(fields: _*)
      }

      override def read(value: JsValue): T = value match {
        case obj: JsObject =>
          if (ctx.isObject) {
            ctx.rawConstruct(Seq.empty)
          } else {
            ctx.construct { param =>
              param.typeclass.read(obj.fields(param.label))
            }
          }
        case js =>
          deserializationError(
            s"Cannot read JSON '$js' as a ${ctx.typeName.full}")
      }
    }

  def dispatch[T](ctx: SealedTrait[JsonFormat, T]): JsonFormat[T] = {
    val typeFieldName = ctx.annotations
      .collectFirst {
        case g: gadt => g.typeFieldName
      }
      .getOrElse("type")

    new JsonFormat[T] {
      override def write(value: T): JsValue = ctx.dispatch(value) { sub =>
        sub.typeclass.write(sub.cast(value)) match {
          case obj: JsObject =>
            JsObject(
              (Map(typeFieldName -> JsString(sub.typeName.short)) ++
                obj.fields).toSeq: _*)
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
                "because serialized type cannot be determined.")
        }

        ctx.subtypes.find(_.typeName.short == typeName) match {
          case Some(tpe) => tpe.typeclass.read(js)
          case None =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName.full} " +
                s"because type '${typeName}' is unsupported.")
        }
      }
    }
  }

  implicit def derivedFormat[T]: RootJsonFormat[T] =
    macro DerivedFormatHelper.derivedFormat[T]

}

object DerivedFormats extends DerivedFormats with BasicFormats

object DerivedFormatHelper {
  import scala.reflect.macros.whitebox._

  /** Utility that converts a magnolia-generated JsonFormat to a RootJsonFormat. */
  def derivedFormat[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T].typeSymbol.asType
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
