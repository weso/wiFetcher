package controllers

import play.api._
import play.api.mvc._
import es.weso.wiFetcher.persistence.VirtuosoLoader

object Application extends Controller {
  
  def index = 
    controllers.FileUploadController.byFileUploadGET
    
  def uploadData(ttlPath : String, reportPath : String, graph : String) = Action  {
    implicit request => 
      val errors = VirtuosoLoader.store
      Ok(views.html.results.loaderResult(errors, ttlPath, reportPath, graph))
  }
  
  def instructions() = Action {
    implicit request =>
      Ok(views.html.instructions.instructions())
  }
  
}