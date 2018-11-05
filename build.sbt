// import Dependencies._

lazy val commonSettings = List(
  organization := "com.snapcardster",
  scalaVersion := "2.12.7",
  version := "0.1"
)

lazy val root = (project in file(".")).
  settings(
    //  assemblySettings : _*,
    inThisBuild(commonSettings),
    mainClass in assembly := Some("karstenbot.Program"),
    assemblyJarName in assembly := "karstenbot.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    name := "KarstenBot"
  )

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.0.0-RC2"

// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "4.0.0-RC2"

// mysql
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.12"

// tests
libraryDependencies += "org.specs2" %% "specs2-core" % "4.3.4" % "test"

// html
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"

// json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"

// svg
libraryDependencies += "org.apache.xmlgraphics" % "batik-transcoder" % "1.7"
