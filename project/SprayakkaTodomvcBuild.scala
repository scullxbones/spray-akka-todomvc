import sbt._
import sbt.Keys._

object SprayakkaTodomvcBuild extends Build {

  lazy val sprayakkaTodomvc = Project(
    id = "spray-akka-todomvc",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Spray-Akka TodoMVc",
      organization := "net.bs",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.0"
    )
  )
}
