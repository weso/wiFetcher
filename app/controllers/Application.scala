package controllers

import play.api._
import play.api.mvc._
import es.weso.wiFetcher.persistence.VirtuosoLoader
import scala.io.Source
import java.io.File

object Application extends Controller {
  
  def index = 
    controllers.FileUploadController.byFileUploadGET
    
  def uploadData(ttlPath : String, reportPath : String, graph : String) = Action  {
    implicit request => 
      val errors = VirtuosoLoader.store
      Ok(views.html.results.loaderResult(errors, ttlPath, reportPath, graph + "/"))
  }
  
  def instructions() = Action {
    implicit request =>
      Ok(views.html.instructions.instructions())
  }
  
  def files(path : String) = Action {
    implicit request => 
      Ok(Source.fromFile(new File("./public/" + path)).mkString).as(extractMimeType(path))
  }
  
  protected def extractMimeType(path : String) : String = {
    val ext = path.substring(path.lastIndexOf('.'), path.length)
    		.replace(".", "")
	println("EXTENSION: " + ext)
	ext match {
      case "csv" => "text/csv"
      case "ttl" => "text/turtle"
      case "n3" => "text/n3"
      case "html" => "text/html"
      case "css" => "text/css"
      case "js" => "application/javascript"
      case "json" => "application/json"
      case "rdf" => "application/rdf+xml"
      case "xlsx" | "xsl" => "application/vnd.ms-excel"
      case _ => "text/plain"
    }
  }
  
}