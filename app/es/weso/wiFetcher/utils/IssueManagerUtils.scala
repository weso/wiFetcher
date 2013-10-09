package es.weso.wiFetcher.utils

import scala.collection.mutable.Queue
import es.weso.wiFetcher.entities.issues._
import org.slf4j.LoggerFactory
import org.slf4j.Logger

case class FilterIssue(
  val message: Option[String] = None,
  val dataset: Option[String] = None,
  val path: Option[String] = None,
  val sheetName: Option[String] = None,
  val col: Option[Int] = None,
  val row: Option[Int] = None,
  val cell: Option[String] = None) {

  def filter(issue: Issue): Boolean = {
    if (dataset.isDefined && ((dataset == issue.dataset) == false))
      return false
    if (path.isDefined && ((path == issue.path) == false))
      return false
    if (sheetName.isDefined && ((sheetName == issue.sheetName) == false))
      return false
    if (col.isDefined)
      if ((col == issue.col) == false)
        return false
    if (row.isDefined && ((row == issue.row) == false))
      return false
    if (cell.isDefined)
      if ((cell == issue.cell) == false)
        return false
    true
  }
}

class IssueManagerUtils() {

  private val issues: Queue[Issue] = Queue.empty
  private val filters: Queue[FilterIssue] = Queue.empty

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def clear: Unit = issues.clear
  def clearFilters: Unit = filters.clear

  def add(issue: Issue): Unit = {

    issue match {
      case e: Error => logger.error(e.message)
      case w: Warn => logger.error(w.message)
    }
    issues += issue

  }

  def addFilter(filter: FilterIssue): Unit = {
    filters += filter
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

  def filteredAsSeq: List[Issue] = {
    val filteredIssues = for {
      issue <- issues.toList
      filter <- filters
      if (filter.filter(issue))
    } yield {
      issue
    }

    issues.filterNot(filteredIssues.contains _).toList

  }

  def asSeq: List[Issue] = issues.toList

}