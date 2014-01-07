package controllers

import play.api._
import play.api.mvc._
import es.weso.wiFetcher.persistence.VirtuosoLoader
import scala.io.Source
import java.io.File
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * This class represent the default controller of the application.
 */
object Application extends Controller {
  
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
  
  /**
   * This action is triggered when a user goes to index path.
   */
  def index = 
    controllers.FileUploadController.byFileUploadGET
    
  /**
   *   This action is triggered when a user upload new data to virtuoso server.
   */  
  def uploadData(ttlPath : String, reportPath : String, graph : String, namespace : String) = Action  {
    implicit request => 
      logger.info("Upload data. IP Address: " + request.remoteAddress)
      val errors = VirtuosoLoader.store
      Ok(views.html.results.loaderResult(errors, ttlPath, reportPath, graph + "/", namespace))
  }
  
  /**
   * This action is triggered when a user wants visit instruction page
   */
  def instructions() = Action {
    implicit request =>
      Ok(views.html.instructions.instructions())
  }
  
  /**
   * This action is triggered when a user wants to view a report or ttl file
   */
  def files(path : String) = Action {
    implicit request => 
      Ok(Source.fromFile(new File("./public/" + path)).mkString).as(extractMimeType(path))
  }
  
  /**
   * This method obtain the extension of a file and return it's mime type.
   */
  protected def extractMimeType(path : String) : String = {
    val ext = path.substring(path.lastIndexOf('.'), path.length)
    		.replace(".", "")
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