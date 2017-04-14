scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17" % "test",
  "org.scalatest" %% "scalatest" % "2.1.5" % "test")

reformatOnCompileSettings

version := "1.0.0"

organization := "io.github.pityka"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

pomExtra in Global := {
  <url>https://pityka.github.io/nspl/</url>
  <scm>
    <connection>scm:git:github.com/pityka/bottleneck</connection>
    <developerConnection>scm:git:git@github.com:pityka/bottleneck</developerConnection>
    <url>github.com/pityka/bottleneck</url>
  </scm>
  <developers>
    <developer>
      <id>pityka</id>
      <name>Istvan Bartha</name>
      <url>https://pityka.github.io/bottleneck/</url>
    </developer>
  </developers>
}
