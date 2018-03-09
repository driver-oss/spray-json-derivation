// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

lazy val sprayJsonDerivation = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := "spray-json-derivation",
    version in ThisBuild := {
      import sys.process._
      ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
    },
    crossScalaVersions := "2.12.4" :: "2.11.12" :: Nil,
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xlint",
      "-Xfatal-warnings"
    ),
    libraryDependencies ++= Seq(
      "io.spray" %%% "spray-json" % "1.3.4",
      "com.propensive" %%% "magnolia" % "0.7.1",
      "org.scalatest" %%% "scalatest" % "3.0.2" % "test"
    )
  )
  .jvmSettings(
    mimaPreviousArtifacts := Set("xyz.driver" %% "spray-json-derivation" % "0.3.1")
  )

lazy val sprayJsonDerivationJVM = sprayJsonDerivation.jvm

lazy val root = (project in file("."))
  .aggregate(sprayJsonDerivationJVM)
  .settings(
    publish := {},
    publishLocal := {}
  )
