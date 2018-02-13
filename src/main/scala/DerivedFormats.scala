import magnolia._
import spray.json._

import scala.language.experimental.macros

trait JsonFormatDerivation extends DefaultJsonProtocol {
  type Typeclass[T] = JsonFormat[T]

  def combine[T](ctx: CaseClass[JsonFormat, T]): JsonFormat[T] = new JsonFormat[T] {
    override def write(value: T): JsValue = {
      val fields: Seq[(String, JsValue)] = ctx.parameters.map { param =>
        param.label -> param.typeclass.write(param.dereference(value))
      }
      JsObject(fields: _*)
    }
    override def read(value: JsValue): T = value match {
      case obj: JsObject =>
        ctx.construct { param =>
          param.typeclass.read(obj.fields(param.label))
        }
      case js =>
        deserializationError(s"Cannot read JSON '$js' as a ${ctx.typeName}")
    }
  }

  def dispatch[T](ctx: SealedTrait[JsonFormat, T]): JsonFormat[T] = new JsonFormat[T] {
    override def write(value: T): JsValue = {
      ctx.dispatch(value) { sub =>
        val obj = sub.typeclass.write(sub.cast(value)).asJsObject
        JsObject((obj.fields ++ Map("type" -> JsString(sub.label))).toSeq: _*)
      }
    }
    override def read(value: JsValue): T = value match {
      case obj: JsObject if obj.fields.contains("type") =>
        val fieldName = obj.fields("type").convertTo[String]

        ctx.subtypes.find(_.label == fieldName) match {
          case Some(tpe) => tpe.typeclass.read(obj)
          case None =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName} because type field '${fieldName}' has an unsupported value.")
        }

      case js =>
        deserializationError(s"Cannot read JSON '$js' as a ${ctx.typeName}")
    }

  }

  implicit def gen[T]: JsonFormat[T] = macro Magnolia.gen[T]

}
object JsonFormatDerivation extends JsonFormatDerivation

trait DerivedFormats extends JsonFormatDerivation
