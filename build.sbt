ThisBuild / scalaVersion := "2.13.10"
ThisBuild / autoScalaLibrary := false
ThisBuild / crossPaths := false
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / organization := "de.bitmarck.bms"
name := (root / name).value

val V = new {
  val logbackClassic = "1.5.16"
  val munit = "1.0.4"
}

lazy val commonSettings: SettingsDefinition = Def.settings(
  version := {
    val Tag = "refs/tags/v?([0-9]+(?:\\.[0-9]+)+(?:[+-].*)?)".r
    sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
      .getOrElse("0.0.1-SNAPSHOT")
  },

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),

  homepage := Some(url("https://base32check.org")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/bitmarck-service/base32check-java"),
      "scm:git@github.com:bitmarck-service/base32check-java.git"
    )
  ),
  developers := List(
    Developer(id = "u016595", name = "Pierre Kisters", email = "pierre.kisters@bitmarck.de", url = url("https://github.com/lhns/"))
  ),

  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % V.logbackClassic % Test,
    "org.scalameta" %% "munit" % V.munit % Test,
  ),

  testFrameworks += new TestFramework("munit.Framework"),

  Compile / doc / sources := Seq.empty,

  publishMavenStyle := true,

  publishTo := sonatypePublishToBundle.value,

  sonatypeCredentialHost := "oss.sonatype.org",

  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    sonatypeCredentialHost.value,
    username,
    password
  )).toList
)

lazy val root: Project = project.in(file("."))
  .settings(commonSettings)
  .settings(
    name := "base32check-java"
  )
