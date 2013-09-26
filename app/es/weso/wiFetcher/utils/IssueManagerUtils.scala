package es.weso.wiFetcher.utils

import scala.collection.mutable.Queue
import es.weso.wiFetcher.entities.issues._
import org.slf4j.LoggerFactory
import org.slf4j.Logger

object IssueManagerUtils {

  private val issues: Queue[Issue] = Queue.empty

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def clear: Unit = issues.clear

  def add(issue: Issue): Unit = {

    issue match {
      case e: Error => logger.error(e.message)
      case w: Warn => logger.error(w.message)
    }
    issues += issue

  }

  def addError(message: String, dataset: Option[String] = None,
    path: Option[String] = None, sheetName: Option[String] = None,
    col: Option[Int] = None, row: Option[Int] = None,
    cell: Option[String] = None): Unit = {

    logger.error(message)
    issues += Error(
      message,
      dataset,
      path,
      sheetName,
      col,
      row,
      cell)

  }

  def addWarn(message: String, dataset: Option[String] = None,
    path: Option[String] = None, sheetName: Option[String] = None,
    col: Option[Int] = None, row: Option[Int] = None,
    cell: Option[String] = None): Unit = {

    logger.info(message)
    Warn(
      message,
      dataset,
      path,
      sheetName,
      col,
      row,
      cell)

  }

  def asSeq: List[Issue] = issues.toList

}