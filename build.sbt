enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

scalaVersion := "2.11.7"

val akkaV = "2.4.1"

val akkaHttpV = "2.0.1"

name := "Spray-Akka TodoMVCs"

organization := "net.bs"

version := "0.2-SNAPSHOT"

mainClass in (Compile, run) := Some("net.bs.Boot")

scalacOptions ++= Seq("-feature", "-deprecation")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

unmanagedResourceDirectories in Compile += file("src/main/webapp")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "spray repo" at "http://repo.spray.io",
  "spray nightlies repo" at "http://nightlies.spray.io"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaHttpV % "test",
  "org.scalatest"     %% "scalatest" % "2.2.5" % "test",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  ("com.typesafe.slick" %% "slick-hikaricp" % "3.1.1")
    .exclude("com.zaxxer", "HikariCP-java6"),
  "org.postgresql"    % "postgresql" % "9.4.1207",
  "ch.qos.logback"    % "logback-classic" % "1.0.7",
  "com.zaxxer"        % "HikariCP" % "2.4.3"
)