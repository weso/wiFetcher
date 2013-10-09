package es.weso.wiFetcher.entities

package issues {

  sealed abstract class Issue {
    val message: String
    val dataset: Option[String]
    val path: Option[String]
    val sheetName: Option[String]
    val col: Option[Int]
    val row: Option[Int]
    val cell: Option[String]
  }

  case class Error(
    message: String,
    dataset: Option[String] = None,
    path: Option[String] = None,
    sheetName: Option[String] = None,
    col: Option[Int] = None,
    row: Option[Int] = None,
    cell: Option[String] = None) extends Issue

  case class Warn(
    message: String,
    dataset: Option[String] = None,
    path: Option[String] = None,
    sheetName: Option[String] = None,
    col: Option[Int] = None,
    row: Option[Int] = None,
    cell: Option[String] = None) extends Issue
}