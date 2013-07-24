import sbt._
import sbt.Keys._

object WiFetcherBuild extends Build {

  val junitV = "4.11"
  val cucumberV = "1.1.2"
  val scalatestV = "2.0.M5b"
  val scalaV = "2.10.1"

  lazy val webindexvalidator = Project(
    id = "wiFetcher",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "wiFetcher",
      organization := "es.weso",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := scalaV,
      libraryDependencies += "junit" % "junit" % junitV,
  
      libraryDependencies += "org.scalatest" %% "scalatest" % scalatestV,
      libraryDependencies += "info.cukes" % "cucumber-jvm" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-core" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-scala" % cucumberV,
      libraryDependencies += "info.cukes" % "cucumber-junit" % cucumberV,
      
      libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.32.0",
      libraryDependencies += "org.apache.poi" % "poi" % "3.9",
      libraryDependencies += "commons-configuration" % "commons-configuration" % "1.9",
      libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.9",
      libraryDependencies += "org.apache.jena" % "jena-arq" % "2.10.1",
      libraryDependencies += "org.apache.jena" % "jena-core" % "2.10.1",
      libraryDependencies += "org.apache.lucene" % "lucene-core" % "4.0.0",
      libraryDependencies += "org.apache.solr" % "solr-core" % "4.0.0",
      libraryDependencies += "es.weso" % "CountryReconciliator" % "1.0",
            
        
      resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")
    )
}
