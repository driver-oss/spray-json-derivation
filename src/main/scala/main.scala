import spray.json._

// product type
case class Foo(x: Int)
case class Bar(foo: Foo, str: String)

// coproduct
sealed trait T
case object A extends T
case class B(a: String) extends T
case class C(x: T) extends T // inception!

object Main extends App with DefaultJsonProtocol with DerivedFormats {

  println("//////////\nProducts:")

  {
    val product = Bar(Foo(42), "hello world")
    val js = product.toJson
    println(js.prettyPrint)
    println(js.convertTo[Bar])
  }

  println("//////////\nCoproducts:")

  {
    val coproduct: T = B("hello wordld") //Seq(C(B("What's up?")), B("a"), A)
    val js = coproduct.toJson
    println(js.prettyPrint)
    println(js.convertTo[T])
  }

}

/*
A potentital danger:

Overriding generated formats is possible (see CustomFormats), however it can be
easy to forget to include the custom formats.
=> In that case, the program will still compile, however it won't use the
   correct format!

Possible workarounds?

  - Require explicit format declarations, i.e. remove implicit from `implicit def
    gen[T] = macro Magnolia.gen[T]` and add `def myFormat = gen[Foo]` to every
    format trait.
    => requires manual code, thereby mostly defeats the advantages of automatic derivation
    => (advantage, no more code duplication since macro is expanded only once)

  - Avoid custom formats.
    => entities become "API objects", which will be hard to upgrade in a backwards-compatible, yet idiomatic way
       (E.g. new fields could be made optional so that they won't be required in json, however the business logic
       may not require them to be optional. We lose some typesafety.)
       => we'd likely have an additional layer of indirection, that will convert "api objects" to "business objects"
          implemented by services
          => Is that a good or bad thing?
*/
