import scala.annotation.implicitNotFound
import scala.concurrent.duration.DurationInt
import akka.actor.Props.apply
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors.AutoremoveActor
import play.api._
import play.api.mvc._
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter) with GlobalSettings {

  override def onStart(app: Application) {
    super.onStart(app)
    /*If application start in a production or development mode (not test), 
    * the daemon will be executed. 
    */
    play.api.Play.mode(app) match {
      case play.api.Mode.Test => // do not schedule anything for Test  
      case _ => autoremoveEARLDaemon(app)
    }

  }

  /**
   * This method executes a daemon that delete all reports and ttl files after
   * 24 hours of their creation
   */
  def autoremoveEARLDaemon(app: Application) {

    Logger.info("Schedulling \"Autoremove TTL-Reports Demon\" every 48h")

    val autoremoveActor = Akka.system(app).actorOf(Props(new AutoremoveActor()))

    Akka.system(app).scheduler.schedule(
      24 hours,
      48 hours,
      autoremoveActor, "autoremoveTTLReports")
  }

}