package controllers

import java.io.File
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.wiFetcher.entities.issues.Issue
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import java.io.ByteArrayOutputStream

object FileUploadController extends Controller {

  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.structureFileGET())
  }

  def byFileUploadPOST() = Action.async(parse.multipartFormData) {
    implicit request =>

      val structure = request.body.file("structure_file").map { file =>
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
        f
      }

      val observations = request.body.file("observations_file").map { file =>
        val f = new File("public/temp/" + file.filename)
        file.ref.moveTo(f, true)
        f
      }

      structure match {
        case Some(s) => observations match {
          case Some(o) => {
            val future = scala.concurrent.Future {
              SpreadsheetsFetcher.loadAll(s, o)
            }

            future.map {
              
              issues => 
                val out = new ByteArrayOutputStream
                issues._1.write(out, "TURTLE")
                Ok(views.html.results.result(out.toString().trim, issues._2))
            }
          }
          case _ =>
            scala.concurrent.Future {
              BadRequest("Onservations file cannot be parsed! Upload it again")
            }
        }
        case _ =>
          scala.concurrent.Future {
            BadRequest("Structure file cannot be parsed! Upload it again")
          }
      }

  }

}