import sbt._
import Keys._
import play.Project._
import templemore.sbt.cucumber.CucumberPlugin
import java.io.PrintWriter

object ApplicationBuild extends Build {

  val AppName = "wiFetcher"
  val AppOrg = "es.weso"
    "git fetch --tags".!!
  val AppVersion = "git describe --abbrev=0 --tags".!!.trim

  val ScalaV = "2.10.2"

  /**
   * Dependancies Versions
   */
  val ConfigV = "1.9"
  val CountryV = "0.3.0-SNAPSHOT"
  val CucumberV = "1.1.4"
  val JenaV = "2.10.1"
  val JunitV = "4.11"
  val NScalaV = "0.6.0"
  val PoiV = "3.9"
  val SeleniumV = "2.35.0"
  val ScalatestV = "2.0.RC1"
  val LuceneV = "4.0.0"
  val TypeConfigV = "1.0.1"
  val OpenCsvV = "2.3"

  val appDependencies = Seq(

    jdbc,
    anorm,
    filters,

    /*Test Dependencies*/
    "junit" % "junit" % JunitV % "test",
    "info.cukes" % "cucumber-jvm" % CucumberV % "test",
    "info.cukes" % "cucumber-core" % CucumberV % "test",
    "info.cukes" % "cucumber-junit" % CucumberV % "test",
    "org.seleniumhq.selenium" % "selenium-java" % SeleniumV % "test",
    
    "org.scalatest" %% "scalatest" % ScalatestV % "test",
    "info.cukes" %% "cucumber-scala" % CucumberV % "test",

    /*Scala Dependencies*/
    "es.weso" % "countryreconciliator_2.10" % CountryV,
    "com.github.nscala-time" %% "nscala-time" % NScalaV,

    /*Java Dependencies*/
    "commons-configuration" % "commons-configuration" % ConfigV,
    "com.typesafe" % "config" % TypeConfigV,
    "org.apache.poi" % "poi" % PoiV,
    "org.apache.poi" % "poi-ooxml" % PoiV,
    "org.apache.jena" % "jena-arq" % JenaV,
    "org.apache.jena" % "jena-core" % JenaV,
    "org.apache.lucene" % "lucene-core" % LuceneV,
    "org.apache.solr" % "solr-core" % LuceneV,
    "net.sf.opencsv" % "opencsv" % OpenCsvV)

  val main = play.Project(AppName, AppVersion, appDependencies).settings(
	playAssetsDirectories <+= baseDirectory / "public/reports",
	playAssetsDirectories <+= baseDirectory / "public/temp",
    scalaVersion := ScalaV,
    /*Extern Repositories*/
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    
    /*Local Repositories*/
    resolvers += Resolver.url("Local Ivy Repository", url("file://" + Path.userHome.absolutePath + "/.ivy2/local/"))(Resolver.ivyStylePatterns),
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    
    templatesImport += "es.weso.wiFetcher.entities.issues._",
    templatesImport += "controllers.FileUploadController._")
    
    
    def writeToFile(fileName: String, value: String) = {
	  	val file = new PrintWriter(new File(fileName))
  		try { file.print(value) } finally { file.close() }
  	}
 
  	writeToFile("conf/app_version.txt", AppVersion)
}
