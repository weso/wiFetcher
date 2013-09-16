import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val junitV = "4.11"
  val cucumberV = "1.1.2"
  val scalatestV = "2.0.M5b"
  val scalaV = "2.10.2"
  val appName         = "wiFetcher"
  val appVersion      = "1.1-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "junit" % "junit" % junitV,
    "org.scalatest" %% "scalatest" % scalatestV,
    "info.cukes" % "cucumber-jvm" % cucumberV,
    "info.cukes" % "cucumber-core" % cucumberV,
    "info.cukes" % "cucumber-scala" % cucumberV,
    "info.cukes" % "cucumber-junit" % cucumberV,
    "org.seleniumhq.selenium" % "selenium-java" % "2.32.0",
    "org.apache.poi" % "poi" % "3.9",
    "commons-configuration" % "commons-configuration" % "1.9",
    "com.typesafe" % "config" % "1.0.1",
    "org.apache.poi" % "poi-ooxml" % "3.9",
    "org.apache.jena" % "jena-arq" % "2.10.1",
    "org.apache.jena" % "jena-core" % "2.10.1",
    "org.apache.lucene" % "lucene-core" % "4.0.0",
    "org.apache.solr" % "solr-core" % "4.0.0",
    "es.weso" %% "countryreconciliator" % "0.2.0-SNAPSHOT"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    resolvers += Resolver.url("Local Ivy Repository", url("file://"+Path.userHome.absolutePath+"/.ivy2/local/"))(Resolver.ivyStylePatterns),
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

}
