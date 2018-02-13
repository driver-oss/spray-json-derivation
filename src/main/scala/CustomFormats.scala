
import spray.json._

trait CustomFormats extends DefaultJsonProtocol {

  implicit val fooFormat: JsonFormat[Foo] = new JsonFormat[Foo] {
    def read(number: JsValue) = number match {
      case JsNumber(x) => Foo(-x.toInt)
      case tpe => sys.error(s"no way I'm reading that type $tpe!")
    }
    def write(number: Foo) = JsNumber(-number.x)
  }

  implicit val z: JsonFormat[B] = new JsonFormat[B] {
    def read(x: JsValue) = B("gone")
    def write(x: B) = JsObject("a" -> JsString("A"))
  }

}
