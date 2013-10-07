package controllers

import java.io.File
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import es.weso.wiFetcher.entities.issues.Issue
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import java.io.ByteArrayOutputStream
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData

object FileUploadController extends Controller {

  case class FileForm(val store: Option[Int], val baseUri: String)

  val fileInputForm: Form[FileForm] = Form(
    mapping(
      "load_file" -> optional(number),
      "base_uri" -> text)(FileForm.apply)(FileForm.unapply))

  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.structureFileGET())
  }

  def byFileUploadPOST() = Action.async(parse.multipartFormData) {
    implicit request =>
      fileInputForm.bindFromRequest.fold(
        errors => concurrentFuture("Structure file cannot be parsed! Upload it again"),
        fileInput => {

          val store = fileInput.store.getOrElse(0) != 0
          val structure = loadFile("structure_file")
          val observations = loadFile("observations_file")
          val baseUri = fileInput.baseUri
          structure match {
            case Some(s) => observations match {
              case Some(o) => {
                val future = scala.concurrent.Future {
                  SpreadsheetsFetcher(s, o)
                }
                future.map {
                  sf =>
                    Ok(views.html.results.result(sf.storeAsTTL(baseUri, store), sf.issues))
                }
              }
              case _ => concurrentFuture("Onservations file cannot be parsed! Upload it again")
            }
            case _ => concurrentFuture("Structure file cannot be parsed! Upload it again")
          }
        })
  }

  private def loadFile(name: String)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    request.body.file(name).map { file =>
      val f = new File("public/temp/" + file.filename)
      file.ref.moveTo(f, true)
      f
    }
  }

  private def concurrentFuture(message: String) = scala.concurrent.Future {
    BadRequest(message)
  }

}
