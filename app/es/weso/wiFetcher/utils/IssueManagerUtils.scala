package es.weso.wiFetcher.utils

import scala.collection.mutable.Queue
import es.weso.wiFetcher.entities.issues._
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * This class represents an issue that contains an error or warning message and
 * it's location
 */
case class FilterIssue(
  val message: Option[String] = None,
  val path: Option[String] = None,
  val sheetName: Option[String] = None,
  val col: Option[Int] = None,
  val row: Option[Int] = None,
  val cell: Option[String] = None) {

  /**
   * This method indicates if an issue should be filtered or no using an issue 
   * as a template
   */
  def filter(issue: Issue): Boolean = {
    if (message.isDefined && ((message == issue.message) == false))
      return false
    if (path.isDefined && ((path == issue.path) == false))
      return false
    if (sheetName.isDefined && ((sheetName == issue.sheetName) == false))
      return false
    if (col.isDefined && ((col == issue.col) == false))
      return false
    if (row.isDefined && ((row == issue.row) == false))
      return false
    if (cell.isDefined && ((cell == issue.cell) == false))
      return false
    true
  }
}

/**
 * This class represents an issue manager. It contains all the generated errors 
 * and warnings that are generated during the execution of the application
 */
class IssueManagerUtils() {

  private val issues: Queue[Issue] = Queue.empty
  private val filters: Queue[FilterIssue] = Queue.empty

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def clear: Unit = issues.clear
  def clearFilters: Unit = filters.clear

  /**
   * This method adds a new issue to the list
   */
  def add(issue: Issue): Unit = {

    issue match {
      case e: Error => logger.error(e.message)
      case w: Warn => logger.error(w.message)
    }
    issues += issue

  }

  /**
   * This method adds a filter to the list
   */
  def addFilter(filter: FilterIssue): Unit = {
    filters += filter
  }

  /**
   * This method adds an error message to the list
   */
  def addError(message: String, path: Option[String] = None,
    sheetName: Option[String] = None, col: Option[Int] = None,
    row: Option[Int] = None, cell: Option[String] = None): Unit = {

    logger.error(message)
    issues += Error(message, path, sheetName, col, row, cell)

  }

  /**
   * This method adds a warning message to the list
   */
  def addWarn(message: String, path: Option[String] = None,
    sheetName: Option[String] = None, col: Option[Int] = None,
    row: Option[Int] = None, cell: Option[String] = None): Unit = {

    logger.info(message)
    issues += Warn(message, path, sheetName, col, row, cell)
  }

  /**
   * This method filters issues list
   */
  def filteredAsSeq: List[Issue] = {
    val filteredIssues = for {
      issue <- issues.toList
      filter <- filters
      if (filter.filter(issue))
    } yield {
      issue
    }
    val finalIssues = for {
    	issue <- issues
    	if(!filteredIssues.contains(issue))
      } yield {
       issue
    }

    finalIssues.toList
  }

  def asSeq: List[Issue] = issues.toList

}