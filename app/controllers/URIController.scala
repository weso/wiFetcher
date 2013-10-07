package controllers

import play.api.mvc.Controller
import play.api.mvc.Action

object URIController extends Controller {

  def byUriGET = Action {
    implicit request =>
      val uri: String = request.queryString.get("path").mkString
      if (uri != null) {
        if (!uri.startsWith("http://")) {
          if (!uri.startsWith("https://")) {
            "http://" + uri
          } else { uri }
        } else { uri }
        Ok(uri)
      } else {
        Ok("Fallo")
      }
  }
}