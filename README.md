[![Build Status](https://travis-ci.org/drivergroup/spray-json-derivation.svg?branch=master)](https://travis-ci.org/drivergroup/spray-json-derivation)
[![Latest version](https://index.scala-lang.org/drivergroup/spray-json-derivation/latest.svg)](https://index.scala-lang.org/drivergroup/spray-json-derivation)
[![Download](https://img.shields.io/maven-central/v/xyz.driver/spray-json-derivation_2.12.svg)](http://search.maven.org/#search|ga|1|xyz.driver%20spray-json-derivation-)

# Spray JSON Format Derivation

This library provides automatic spray JsonFormats for any `case class`
and children of `sealed trait`s.

It uses the [Magnolia](http://magnolia.work/) ([source
code](https://github.com/propensive/magnolia)) typeclass derivation library
to implicitly generate JSON formats for any product type. Magnolia
integrates with spray so seamlessly that it is almost not worth the
effort to publish this project as a full fledged repository; a single
gist with the contents of
[DerivedFormats.scala](src/main/scala/DerivedFormats.scala) would
demonstrate almost all functionality.

## Getting Started

Spray JSON Format Derivation is published to maven central as a Scala
library. Include it in sbt by adding the following snippet to your
build.sbt:

```scala
libraryDependencies += "xyz.driver" %% "spray-json-derivation" % "<latest version>"
```

Define some case classes and mix `ImplicitDerivedFormats` into your JSON
protocol stack. That's it.

```scala
import spray.json._
import xyz.driver.json.ImplicitDerivedFormats

object Main extends App with DefaultJsonProtocol with ImplicitDerivedFormats {
  
  // Simple case classes

  case class A(x: Int)
  case class B(a: A, str: String)

  println(B(A(42), "hello world").toJson.prettyPrint)
  // {
  //   "a": {
  //     "x": 42
  //   },
  //   "str": "hello world"
  // }


  // Sealed traits

  sealed trait X
  case class Y(x: Int) extends X
  case class Z(y: Y, str: String) extends X

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

It is also possible to summon derived formats explicitly by mixing in `DerivedFormats`instead of `ImplicitDerivedFormats`:
```scala
import spray.json._
import xyz.driver.json.DerivedFormats

object Main extends App with DefaultJsonProtocol with DerivedFormats {

  case class A(x: Int)
  case class B(a: A, str: String)

  implicit val bFormat: RootJsonFormat[B] = jsonFormat[B]

  println(B(A(42), "hello world").toJson.prettyPrint)
```
This will have the additional benefit of outputting a stacktrace in case a format cannot be derived, hence making debugging
much easier.

## Documentation
Check out the main file
[DerivedFormats.scala](src/main/scala/DerivedFormats.scala) and the
[test suite](src/test/scala/ProductTypeFormatTests.scala) for a complete
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
