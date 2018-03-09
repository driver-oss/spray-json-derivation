organization in ThisBuild := "xyz.driver"
licenses in ThisBuild := Seq(
  ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
homepage in ThisBuild := Some(
  url("https://github.com/drivergroup/spray-json-derivation"))
publishMavenStyle in ThisBuild := true
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/drivergroup/spray-json-derivation"),
    "scm:git@github.com:drivergroup/spray-json-derivation.git"
  )
)

developers in ThisBuild := List(
  Developer(
    id = "jodersky",
    name = "Jakob Odersky",
    email = "jakob@driver.xyz",
    url = url("https://driver.xyz")
  )
)
