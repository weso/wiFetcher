import sbt._
import sbt.Keys._

object WebindexvalidatorBuild extends Build {

  val junitV = "4.11"
  val cucumberV = "1.1.3"
  val scalatestV = "2.0.M5b"
  val scalaV = "2.10.1"

  lazy val webindexvalidator = Project(
    id = "webindex-validator",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "WebIndex-Validator",
      organization := "webindex-validator",
      version := "0.10-SNAPSHOT",
      scalaVersion := scalaV,
      libraryDependencies += "junit" % "junit" % junitV,
      //Due to
      libraryDependencies += "org.scalatest" %% "scalatest" % scalatestV,
      libraryDependencies += "info.cukes" % "cucumber-jvm" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-core" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-scala" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-junit" % cucumberV,
      
      libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.32.0",
        
      resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")
    )
}