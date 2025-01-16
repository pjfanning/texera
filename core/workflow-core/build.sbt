/////////////////////////////////////////////////////////////////////////////
// Project Settings
/////////////////////////////////////////////////////////////////////////////

name := "workflow-core"
organization := "edu.uci.ics"
version := "0.1.0"
scalaVersion := "2.13.12"

enablePlugins(JavaAppPackaging)

// Enable semanticdb for Scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Manage dependency conflicts by always using the latest revision
ThisBuild / conflictManager := ConflictManager.latestRevision

// Restrict parallel execution of tests to avoid conflicts
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)


/////////////////////////////////////////////////////////////////////////////
// Compiler Options
/////////////////////////////////////////////////////////////////////////////

// Scala compiler options
Compile / scalacOptions ++= Seq(
  "-Xelide-below", "WARNING",       // Turn on optimizations with "WARNING" as the threshold
  "-feature",                       // Check feature warnings
  "-deprecation",                   // Check deprecation warnings
  "-Ywarn-unused:imports"           // Check for unused imports
)


/////////////////////////////////////////////////////////////////////////////
// ScalaPB Configuration
/////////////////////////////////////////////////////////////////////////////

// Exclude some proto files
PB.generate / excludeFilter := "scalapb.proto"

// Set the protoc version for ScalaPB
ThisBuild / PB.protocVersion := "3.19.4"

// ScalaPB code generation for .proto files
Compile / PB.targets := Seq(
  scalapb.gen(singleLineToProtoString = true) -> (Compile / sourceManaged).value
)

// Mark the ScalaPB-generated directory as a generated source root
Compile / managedSourceDirectories += (Compile / sourceManaged).value

// ScalaPB library dependencies
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0"  // For ScalaPB 0.11.x
)

// Enable protobuf compilation in Test
Test / PB.protoSources += PB.externalSourcePath.value


/////////////////////////////////////////////////////////////////////////////
// Test-related Dependencies
/////////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,                  // ScalaMock
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,                 // ScalaTest
  "junit" % "junit" % "4.13.2" % Test,                              // JUnit
  "com.novocode" % "junit-interface" % "0.11" % Test                // SBT interface for JUnit
)


/////////////////////////////////////////////////////////////////////////////
// Jackson-related Dependencies
/////////////////////////////////////////////////////////////////////////////

val jacksonVersion = "2.15.1"
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,        // Jackson Databind
  "com.fasterxml.jackson.module" % "jackson-module-kotlin" % jacksonVersion % Test,   // Jackson Kotlin Module
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion % Test, // Jackson JDK8 Datatypes
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion % Test, // Jackson JSR310
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion % Test,   // Jackson Joda
  "com.fasterxml.jackson.module" % "jackson-module-jsonSchema" % jacksonVersion,      // JSON Schema Module
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,           // Scala Module
  "com.fasterxml.jackson.module" % "jackson-module-no-ctor-deser" % jacksonVersion     // No Constructor Deserializer
)


/////////////////////////////////////////////////////////////////////////////
// MongoDB-related Dependencies
/////////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "org.mongodb" % "mongodb-driver-sync" % "5.0.0",               // MongoDB driver
  "org.apache.commons" % "commons-jcs3-core" % "3.2"             // Apache Commons JCS
)

/////////////////////////////////////////////////////////////////////////////
// Arrow related
val arrowVersion = "14.0.1"
val arrowDependencies = Seq(
  // https://mvnrepository.com/artifact/org.apache.arrow/flight-grpc
  "org.apache.arrow" % "flight-grpc" % arrowVersion,
  // https://mvnrepository.com/artifact/org.apache.arrow/flight-core
  "org.apache.arrow" % "flight-core" % arrowVersion
)

libraryDependencies ++= arrowDependencies

/////////////////////////////////////////////////////////////////////////////
// Iceberg-related Dependencies
/////////////////////////////////////////////////////////////////////////////
val excludeJersey = ExclusionRule(organization = "com.sun.jersey")
val excludeGlassfishJersey = ExclusionRule(organization = "org.glassfish.jersey")
val excludeSlf4j = ExclusionRule(organization = "org.slf4j")
val excludeJetty = ExclusionRule(organization = "org.eclipse.jetty")
val excludeJsp = ExclusionRule(organization = "javax.servlet.jsp")
val excludeXmlBind = ExclusionRule(organization = "javax.xml.bind")
val excludeJackson = ExclusionRule(organization = "com.fasterxml.jackson.core")
val excludeJacksonModule = ExclusionRule(organization = "com.fasterxml.jackson.module")

libraryDependencies ++= Seq(
  "org.apache.iceberg" % "iceberg-api" % "1.7.1",
  "org.apache.iceberg" % "iceberg-parquet" % "1.7.1" excludeAll(
    excludeJackson,
    excludeJacksonModule
  ),
  "org.apache.iceberg" % "iceberg-core" % "1.7.1" excludeAll(
    excludeJackson,
    excludeJacksonModule
  ),
  "org.apache.iceberg" % "iceberg-data" % "1.7.1" excludeAll(
    excludeJackson,
    excludeJacksonModule
  ),
  "org.apache.hadoop" % "hadoop-common" % "3.3.1" excludeAll(
    excludeXmlBind,
    excludeGlassfishJersey,
    excludeJersey,
    excludeSlf4j,
    excludeJetty,
    excludeJsp,
    excludeJackson,
    excludeJacksonModule
  ),
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.1" excludeAll(
    excludeXmlBind,
    excludeGlassfishJersey,
    excludeJersey,
    excludeSlf4j,
    excludeJetty,
    excludeJsp,
    excludeJackson,
    excludeJacksonModule
  ),
)

/////////////////////////////////////////////////////////////////////////////
// Additional Dependencies
/////////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "com.github.sisyphsu" % "dateparser" % "1.0.11",                    // DateParser
  "com.google.guava" % "guava" % "31.1-jre",                          // Guava
  "org.ehcache" % "sizeof" % "0.4.3",                                 // Ehcache SizeOf
  "org.jgrapht" % "jgrapht-core" % "1.4.0",                           // JGraphT Core
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",          // Scala Logging
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.13.0.202109080827-r",  // jgit
  "org.yaml" % "snakeyaml" % "1.30",                                  // yaml reader (downgrade to 1.30 due to dropwizard 1.3.23 required by amber)
  "org.apache.commons" % "commons-vfs2" % "2.9.0",                     // for FileResolver throw VFS-related exceptions
  "io.lakefs" % "sdk" % "1.48.0"
)