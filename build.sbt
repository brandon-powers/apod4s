import Version._

name := "apod"
version := "0.1"
scalaVersion := Version.scala

scalacOptions in ThisBuild ++= Seq("-feature")

libraryDependencies := Seq(
  "org.http4s" %% "http4s-dsl" % Version.http4s,
  "org.http4s" %% "http4s-blaze-client" % Version.http4s,
  "com.typesafe" % "config" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "io.circe" %% "circe-core" % "0.12.3",
  "io.circe" %% "circe-generic" % "0.12.3",
  "io.circe" %% "circe-parser" % "0.12.3",
  "org.http4s" %% "http4s-circe" % Version.http4s,
  "io.circe" %% "circe-generic-extras" % "0.8.0"
)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)

