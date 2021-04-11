name := "high-level-akka-http"

version := "0.1"

scalaVersion := "2.13.5"

val akkaVersion = "2.6.13"
val akkaHttpVersion = "10.2.4"
val scalaTestVersion = "3.2.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  // "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  // JWT
  "com.pauldijou" %% "jwt-spray-json" % "5.0.0"
)
