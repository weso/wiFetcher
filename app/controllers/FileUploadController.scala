package controllers

import java.io.File

import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.validation.Constraints.max
import play.api.data.validation.Constraints.min
import play.api.data.validation.Constraints.nonEmpty
import play.api.libs.Files.TemporaryFile
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.MultipartFormData
import play.api.mvc.Request

object FileUploadController extends Controller {

  case class FileForm(val baseUri: String, val year: Int, val namespace: String, val store: Option[Int])

  val fileInputForm: Form[FileForm] = Form(
    mapping(
      "base_uri" -> text.verifying(nonEmpty),
      "year_uri" -> number.verifying(min(2001), max(2013)),
      "namespace_uri" -> text.verifying(nonEmpty),
      "load_file" -> optional(number))(FileForm.apply)(FileForm.unapply))

  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.upload(fileInputForm))
  }

  def byFileUploadPOST() = Action.async(parse.multipartFormData) {
    implicit request =>
      fileInputForm.bindFromRequest.fold(
        formWithErrors => scala.concurrent.Future {
          Ok(views.html.file.upload(formWithErrors))
        },
        fileInput => {
          val store = fileInput.store.getOrElse(0) != 0
          val structure = loadFile("structure_file")
          val observations = loadFile("observations_file")

          val baseUri = if (fileInput.baseUri.endsWith("/"))
            fileInput.baseUri.substring(0, fileInput.baseUri.length - 1)
          else fileInput.baseUri

          /*val uri = new StringBuilder(baseUri).append("/")
            .append(fileInput.namespace).append("/v").append(fileInput.year).toString*/
          val year = "v" + fileInput.year.toString
          structure match {
            case Some(s) => observations match {
              case Some(o) => {
                val future = scala.concurrent.Future {
                  SpreadsheetsFetcher(s, o)
                }
                future.map {
                  sf =>
                    Ok(views.html.results.result(sf.storeAsTTL(baseUri, 
                        fileInput.namespace, year), sf.issues))
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
