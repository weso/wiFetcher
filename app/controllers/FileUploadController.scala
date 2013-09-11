package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils

object FileUploadController extends Controller {
  
  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.defaultFileGET())
  }
  
  def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map{file =>
      	val contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(
      	    file.ref.file))
      	//TODO Call to SpreadsheetFetcher to parse the data
      	Ok("Parsear " + file.filename)
      }
    BadRequest("")
  }

}