package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import java.io.FileInputStream
import java.io.File

object FileUploadController extends Controller {
  
  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.defaultFileGET())
  }
  
  def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map{file =>
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
//      	val contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(
//      	    file.ref.file))
      	SpreadsheetsFetcher.loadStructure(f)
      	Ok("Peto")
      }
    BadRequest("")
  }

}