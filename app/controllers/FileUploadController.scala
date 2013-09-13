package controllers

import java.io.File

import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import play.api.mvc.Action
import play.api.mvc.Controller

object FileUploadController extends Controller {
  
  def byStructureFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.structureFileGET())
  }
  
  def byStructureFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map{file =>
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
//      	val contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(
//      	    file.ref.file))
        SpreadsheetsFetcher.loadStructure(f)
        Ok(views.html.file.observationsFileGET())
      }.getOrElse{
          Ok(views.html.file.structureFileGET("Structure file cannot be " +
          		"parsed! Upload it again"))
      }
  }
  
  def byObservationFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map{ file =>
        println("ObservationFileUploadPOST")
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
        SpreadsheetsFetcher.loadObservations(f)
        Ok("Todo bien")
      }.getOrElse {
        Ok("Algo fallo")
      }
  }

}