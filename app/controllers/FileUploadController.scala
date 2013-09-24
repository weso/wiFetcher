package controllers

import java.io.File

import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import play.api.mvc.Action
import play.api.mvc.Controller

object FileUploadController extends Controller {
  
  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.structureFileGET())
  }
  
  def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("structure_file").map{file =>
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
        SpreadsheetsFetcher.loadStructure(f)
      }.getOrElse{
          Ok("Structure file cannot be " +
          		"parsed! Upload it again")
      }
      request.body.file("observations_file").map{file => 
      	val f = new File("public/temp/" + file.filename)
      	file.ref.moveTo(f, true)
      	SpreadsheetsFetcher.loadObservations(f)
      	Ok("All OK")
      }.getOrElse{
        Ok("Structure file cannot be " +
          		"parsed! Upload it again")
      }
      //TODO remove temporary files
      Ok("All OK")
  }

}