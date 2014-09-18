import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AccesslogBuild extends Build {
  lazy val versions = Seq(
      name := "accesslog",
      organization := "com.uzabase.newspicks",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.4"
    )

  lazy val accesslog = Project(
    id = "accesslog",
    base = file("."),
    settings = Project.defaultSettings ++ versions ++ assemblySettings ++ Seq(
      resolvers ++= Seq(
          "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
          Resolver.sonatypeRepo("public")
      ),
      libraryDependencies ++= Seq(
          "com.typesafe.akka" % "akka-actor_2.10" % "2.3.3",
          "com.amazonaws" % "aws-java-sdk" % "1.7.12",
          "org.json4s" % "json4s-native_2.10" % "3.2.10",
          "org.codehaus.jackson" % "jackson-mapper-lgpl" % "1.9.13",
          "io.spray" % "spray-json_2.10" % "1.2.6",
          "io.spray" % "spray-client" % "1.3.1",
          "org.elasticsearch" % "elasticsearch" % "1.2.1",
          "com.github.scopt" %% "scopt" % "3.2.0",
          "org.joda" % "joda-convert" % "1.6",
          "com.typesafe" %% "scalalogging-slf4j" % "1.0.0",
          "ch.qos.logback" % "logback-classic" % "1.1.2",
          "org.specs2" % "specs2_2.10" % "2.3.10" % "test",
          "org.scalamock" % "scalamock-specs2-support_2.10" % "3.0.1" % "test"
      )
    )
  )

  scalacOptions in Test ++= Seq("-Yrangepos")
  resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
}