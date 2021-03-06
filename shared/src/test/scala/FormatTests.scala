package spray.json

import org.scalatest._

trait FormatTests { self: FlatSpec =>

  def checkRoundtrip[A: RootJsonFormat](a: A, expectedJson: String) = {
    it should "serialize to the expected JSON value" in {
      val expected: JsValue = expectedJson.parseJson
      assert(a.toJson == expected)
    }

    it should "serialize then deserialize back to itself" in {
      val reread = a.toJson.convertTo[A]
      assert(reread == a)
    }
  }

}
