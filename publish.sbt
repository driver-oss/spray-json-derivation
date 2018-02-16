organization := "xyz.driver"
licenses in ThisBuild := Seq(
  ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
homepage in ThisBuild := Some(
  url("https://github.com/drivergroup/spray-json-derivation"))
publishMavenStyle in ThisBuild := true
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/drivergroup/spray-json-derivation"),
    "scm:git@github.com:drivergroup/spray-json-derivation.git"
  )
)

developers := List(
  Developer(
    id = "jakob@driver.xyz",
    name = "Jakob Odersky",
    email = "jakob@driver.xyz",
    url = url("https://driver.xyz")
  )
)
