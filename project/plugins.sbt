ivyLoggingLevel := UpdateLogging.Quiet

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.6.0-RC3")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.18")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.4.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.4.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.7")
