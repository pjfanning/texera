import scala.collection.Seq

name := "workflow-computing-unit-managing-service"
organization := "edu.uci.ics"
version := "0.1.0"

enablePlugins(JavaAppPackaging)

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

// Dependency Versions
val dropwizardVersion = "4.0.7"

// Dependencies
libraryDependencies ++= Seq(
  "io.dropwizard" % "dropwizard-core" % dropwizardVersion,
  "io.kubernetes" % "client-java" % "21.0.0",
  "org.jooq" % "jooq" % "3.14.16",
  "com.typesafe" % "config" % "1.4.1",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M6",
  "com.lihaoyi" %% "upickle" % "3.1.0",
  "org.yaml" % "snakeyaml" % "1.30",                    // for reading storage config yaml file
  "com.typesafe" % "config" % "1.4.2"
)

// Compiler Options
Compile / scalacOptions ++= Seq(
  "-Xelide-below", "WARNING",
  "-feature",
  "-deprecation",
  "-Ywarn-unused:imports"
)