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
import java.util.Date
import es.weso.wiFetcher.entities.issues._
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * This object is a controller to process excel files when they are loaded
 */
object FileUploadController extends Controller {
  
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  /**
   * This case class represent the form that users have to fill in order to
   * parse excel files
   */
  case class FileForm(val baseUri: String, val year: Int, val namespace: String/*, val store: Option[Int]*/)

  /**
   * This val represents the form
   */
  val fileInputForm: Form[FileForm] = Form(
    mapping(
      "base_uri" -> text.verifying(nonEmpty),
      "year_uri" -> number.verifying(min(2001), max(2013)),
      "namespace_uri" -> text.verifying(nonEmpty)/*,
      "load_file" -> optional(number)*/)(FileForm.apply)(FileForm.unapply))

  /**
   * This action is triggered when a user wants to access to the form, and have
   * to return the corresponding view
   */
  def byFileUploadGET() = Action {
    implicit request =>
      Ok(views.html.file.upload(fileInputForm))
  }

  /**
   * This action is triggered when a user fills the form a send data to the 
   * application. It has to validate data and parse received data.
   */
  def byFileUploadPOST() = Action.async(parse.multipartFormData) {
    implicit request =>
      logger.info("Parse excel file. IP Address: " + request.remoteAddress)
      //If there is a problem with data
      fileInputForm.bindFromRequest.fold(
        formWithErrors => scala.concurrent.Future {
          Ok(views.html.file.upload(formWithErrors))
        },
        //If all is OK
        fileInput => {
          //Save both files in a local folder in order to parse
          val structure = loadFile("structure_file")
          val observations = loadFile("observations_file")

          val baseUri = if (fileInput.baseUri.endsWith("/"))
            fileInput.baseUri.substring(0, fileInput.baseUri.length - 1)
          else fileInput.baseUri

          val year = "v" + fileInput.year.toString
          //We check if structure and observation are in the same file or no
          structure match {
            case Some(s) => load(observations, structure, baseUri, fileInput, year/*, store*/)
            case None => load(observations, observations, baseUri, fileInput, year/*, store*/)
            case _ => concurrentFuture("Structure file cannot be parsed! Upload it again")
          }
        })
  }
  
  /**
   * This auxiliary method receive all valid data and create a 
   * SpreadsheetFetcher object in order to process files
   */
  private def load(observations : Option[File], structure : Option[File], 
      baseUri : String, fileInput : FileForm, year : String/*, store : Boolean*/) = {
    //If is a valid file
    observations match {
              case Some(o) => {
                //Process data
                val future = scala.concurrent.Future {
                  SpreadsheetsFetcher(structure.get, o)
                }
                //If all is OK
                future.map {
                  sf =>
                    //Obtain the timestamp
                    val timestamp = new Date().getTime 
                    //Create ttl file with loaded data
                    val path = sf.storeAsTTL(baseUri, 
                        fileInput.namespace, year/*, store*/, timestamp)
                    //Create report file
                    val results : (Seq[Issue], String) = sf.saveReport(timestamp)
                    //Obtain the name of the graph
                    val graph = new StringBuilder(baseUri).append("/")
                    	.append(fileInput.namespace).append("/v")
                    	.append(fileInput.year).toString
                    //Generate the path of the computations
                    val computationPath = new StringBuilder("temp/")
                    	.append(fileInput.namespace).append("-computations.ttl").toString
                    //Return results view
                    Ok(views.html.results.result(path, results._1, results._2, graph, computationPath))
                }
              }
              case _ => concurrentFuture("Observations file cannot be parsed! Upload it again")
            }
  }

  /**
   * This auxiliary method save uploaded files in a local folder in order to
   * process them
   */
  private def loadFile(name: String)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    request.body.file(name).map { file =>
      val f = new File("public/temp/" + file.filename)
      file.ref.moveTo(f, true)
      f
    }
  }

  /**
   * This auxiliary method return a bad request view showing a message that is 
   * given as a parameter
   */
  private def concurrentFuture(message: String) = scala.concurrent.Future {
    BadRequest(message)
  }

}
