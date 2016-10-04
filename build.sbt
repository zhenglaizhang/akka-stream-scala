name := """akka-stream-scala"""

version := "1.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.11",
  //  "com.typesafe.akka" %% "akka-http-core" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-jackson-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-xml-experimental" % "2.4.11"
)

//fork in run := true

// http://stackoverflow.com/questions/5137460/sbt-stop-run-without-exiting
//cancelable in Global := true

/*
Alternatively if you want to set it only for a single session, while in sbt console, you can write set scalacOptions += "-feature", this setting is applied immediately, no need to reload or restart sbt console.
*/
scalacOptions ++= Seq("-feature", "-deprecation")
