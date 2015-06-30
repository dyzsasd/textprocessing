name := "textprocessing"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies  ++= {
  val sprayV = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.10",  // for ByteString

    "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-RC2",

    "io.spray" %% "spray-client" % "1.3.3",

    "com.typesafe.play" % "play-json_2.11" % "2.4.1"

  )
}

resolvers ++= Seq(

  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repo" at "http://repo.spray.io"
)

Revolver.settings
