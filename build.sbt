organization := "de.bitmarck.bms"
name := "base32check-java"
version := "0.0.2-SNAPSHOT"

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8"
)

scalaVersion := "2.13.1"
crossPaths := false

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://base32check.org"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/bitmarck-service/base32check-scala"),
    "scm:git@github.com:bitmarck-service/base32check-scala.git"
  )
)
developers := List(
  Developer(id = "u016595", name = "Pierre Kisters", email = "pierre.kisters@bitmarck.de", url = url("https://github.com/LolHens/"))
)

libraryDependencies ++= Seq(
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
)


Compile / doc / sources := Seq.empty

version := {
  val tagPrefix = "refs/tags/"
  sys.env.get("CI_VERSION").filter(_.startsWith(tagPrefix)).map(_.drop(tagPrefix.length)).getOrElse(version.value)
}

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  username,
  password
)).toList
