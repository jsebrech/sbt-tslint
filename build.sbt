sbtPlugin := true
organization := "name.de-vries"
name := "sbt-tslint"
version := "3.15.1"

homepage := Some(url("https://github.com/joost-de-vries/sbt-tslint"))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.10.6"

incOptions := incOptions.value.withNameHashing(true)
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

resolvers ++= Seq(
    Resolver.typesafeRepo("releases"),
    Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
    Resolver.sonatypeRepo("releases"),
    Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "org.webjars" % "webjars-locator" % "0.32",
  "org.webjars" % "webjars-locator-core" % "0.32",

  "org.webjars.npm" % "typescript" % "2.0.10",
  "org.webjars.npm" % "tslint" % "3.15.1",
  "org.webjars.npm" % "minimatch" % "3.0.0",
  "org.webjars" % "strip-json-comments" % "1.0.2-1",
  "org.webjars.npm" % "sprintf-js" % "1.0.3",  //used by codelyzer,

  "org.webjars" % "npm" % "3.9.3"

)

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.1.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.0")

publishMavenStyle := false
bintrayRepository in bintray := "sbt-plugins"
bintrayOrganization in bintray := None
bintrayVcsUrl := Some("git@github.com:joost-de-vries/sbt-tslint.git")

scriptedSettings

scriptedLaunchOpts += s"-Dproject.version=${version.value}"
