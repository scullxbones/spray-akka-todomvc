import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtStartScript

object SprayakkaTodomvcBuild extends Build {

  lazy val sprayakkaTodomvc = Project(
    id = "spray-akka-todomvc",
    base = file("."),
    settings = Project.defaultSettings ++ SbtStartScript.startScriptForClassesSettings ++ Seq(
      name := "Spray-Akka TodoMVCs",
      organization := "net.bs",
      version := "0.1-SNAPSHOT",
      mainClass in (Compile, run) := Some("net.bs.Boot"),
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq("-feature", "-deprecation"),

		resolvers ++= Seq(
		  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
		  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
		  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
		  "spray repo" at "http://repo.spray.io",
		  "spray nightlies repo" at "http://nightlies.spray.io"
		),
      libraryDependencies ++= Seq(
		  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
		  "com.typesafe.akka" %% "akka-slf4j" % "2.1.0",
		  "io.spray" % "spray-can" % "1.1-20130207",
		  "io.spray" % "spray-routing" % "1.1-20130207",
		  "io.spray" %%  "spray-json" % "1.2.3",
		  "com.typesafe.slick" %% "slick" % "1.0.0",
		  "com.h2database" % "h2" % "1.3.170",
		  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
		  "ch.qos.logback" % "logback-classic" % "1.0.7",
		  "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test",
		  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
		  "org.mockito" % "mockito-all" % "1.9.5" % "test",
		  "io.spray" % "spray-testkit" % "1.1-20130207" % "test",
		  "junit" % "junit" % "4.11" % "test"
      )
    )
  )
}
