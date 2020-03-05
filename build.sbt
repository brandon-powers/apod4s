name := "nasa4s"
version := "0.1"
scalaVersion := Version.scala
scalacOptions in ThisBuild ++= Seq("-feature", "-language:higherKinds")

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := Version.scala,
  addCompilerPlugin(CompilerPlugins.paradise),
  scalacOptions ++= Seq(
    "-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-Ypartial-unification",
    "-Ywarn-unused:imports"
    )
  )

lazy val `nasa4s` = (project in file("."))
  .settings(
    commonSettings,
    name := "nasa4s",
    libraryDependencies ++= Dependencies.nasa4s
    )

