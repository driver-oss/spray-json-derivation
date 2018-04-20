[![Build Status](https://travis-ci.org/drivergroup/spray-json-derivation.svg?branch=master)](https://travis-ci.org/drivergroup/spray-json-derivation)
[![Latest version](https://index.scala-lang.org/drivergroup/spray-json-derivation/latest.svg)](https://index.scala-lang.org/drivergroup/spray-json-derivation)
[![Download](https://img.shields.io/maven-central/v/xyz.driver/spray-json-derivation_2.12.svg)](http://search.maven.org/#search|ga|1|xyz.driver%20spray-json-derivation-)

# Spray JSON Format Derivation

This library provides automatic
[spray-json](https://github.com/spray/spray-json) `RootJsonFormat`s
for any `case class` and children of `sealed trait`s.

It is available (and makes spray-json available) for Scala, ScalaJS and Scala Native.

It uses the [Magnolia](http://magnolia.work/) ([source
code](https://github.com/propensive/magnolia)) typeclass derivation
library to implicitly generate JSON formats for any product
type. Magnolia integrates with spray seamlessly, to such a point that
the contents of
[DerivedFormats.scala](shared/src/main/scala/DerivedFormats.scala) provide
almost all functionality.

## Getting Started

Spray JSON Format Derivation is published to maven central as a Scala
library (for Scala version 2.12 and 2.11, including support for ScalaJS
and Scala Native). Include it in sbt by adding the
following snippet to your build.sbt:

```scala
libraryDependencies += "xyz.driver" %% "spray-json-derivation" % "<latest version>"
```

### Basic Usage

Define some case classes and mix `DerivedFormats` into your JSON
protocol stack. Formats can now be summoned with the `jsonFormat[A]`
helper method. E.g.

```scala
import spray.json._

// case classes
case class A(x: Int)
case class B(a: A, str: String)

// sealed traits
sealed trait X
case class Y(x: Int) extends X
case class Z(y: Y, str: String) extends X

object MyProtocol extends DefaultJsonProtocol with DerivedFormats {
  implicit val bFormat: RootJsonFormat[B] = jsonFormat[B] // [1]
  implicit val xFormat: RootJsonFormat[X] = jsonFormat[X]
}

// [1]: Note that no format was specified for A, although B contains an A.
//      In general, formats of children are not required to be defined
//      explicitly if the child is itself a case class. However they can still
//      be overriden if desired.

object Main extends App {
  import MyProtocol._

  println(B(A(42), "hello world").toJson.prettyPrint)
  // {
  //   "a": {
  //     "x": 42
  //   },
  //   "str": "hello world"
  // }

  println(Seq[X](Z(Y(42), "foo"), Y(2)).toJson.prettyPrint)
  // [{
  //   "type": "Z",
  //   "y": {
  //     "x": 42
  //   },
  //   "str": "foo"
  // }, {
  //   "type": "Y",
  //   "x": 2
  // }]

}
```

### Error Handling

In case a format cannot be derived, e.g. if a child is not a case
class and does not have a format available, Magnolia will report a
detailed stack trace about which format could not be found. This is
most useful for deeply nested structures, where Scala's conventional
implicit lookup would simply fail without giving a detailed error
message. For example:

```scala
import spray.json._

class X() // note that X is not a case class, hence a format cannot be derived
case class Y(x: X)
case class Z(y: Y, w: Int)

object MyProtocol extends DefaultJsonProtocol with DerivedFormats {
  implicit val fmt: RootJsonFormat[Z] = jsonFormat[Z]
}
// magnolia: could not find JsonFormat.Typeclass for type ProductTypeFormatTests.this.X
//      in parameter 'x' of product type ProductTypeFormatTests.this.Y
//      in parameter 'y' of product type ProductTypeFormatTests.this.Z
//    implicit val fmt: RootJsonFormat[Z] = jsonFormat[Z]
```

### Implicit Derived Formats

It is also possible to summon derived formats implicitly by mixing in
`ImplicitDerivedFormats`instead of `DerivedFormats`. This makes it
possible to use derived formats without explicitly creating them
first:

```scala
import spray.json._

object Main extends App with DefaultJsonProtocol with ImplicitDerivedFormats {

  case class A(x: Int)
  case class B(a: A, str: String)

  println(B(A(42), "hello world").toJson.prettyPrint)

}
```

Although this variant of using the library provides the most power and
requires the least amount of boilerplate, it does come with a couple
of drawbacks to consider:

1. Error handling falls back to the standard implicit lookup
   mechanism. Magnolia does no longer show a stack trace if a child
   format cannot be found.

2. Macros that enable format derivation are expanded at every call
   site. In other words, the compiler will try to create JSON formats
   at every point where an implicit `RootJsonFormat` is required for a
   case class. This can increase compile times and binary size if
   formats are required in many locations.

## Documentation

Check out the main file
[DerivedFormats.scala](shared/src/main/scala/DerivedFormats.scala) and the
[test suite](shared/src/test/scala/ProductTypeFormatTests.scala) for a complete
overview of the project.

## Development

This project uses sbt. It is set up to auto-release when a tag is
pushed to the main repository.

## Copying

Copyright 2018 Driver Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
