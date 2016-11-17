name := "insatiable-birdy"

version := "0.1.0"

scalaVersion := "2.11.8"

lazy val root = project in file(".")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.hunorkovacs" %% "koauth" % "1.1.0"
    exclude("com.typesafe.akka", "akka-actor_2.11")
    exclude("org.specs2", "specs2_2.11"),
  "io.circe" %% "circe-core" % "0.6.0",
  "io.circe" %% "circe-generic" % "0.6.0",
  "io.circe" %% "circe-parser" % "0.6.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
