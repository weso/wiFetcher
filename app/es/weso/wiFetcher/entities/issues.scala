package es.weso.wiFetcher.entities

package issues {

  sealed abstract class Issue(
    val _message: String,
    val _dataset: Option[String],
    val _path: Option[String],
    val _sheetName: Option[String],
    val _col: Option[Int],
    val _row: Option[Int],
    val _cell: Option[String])

  case class Error(
    message: String,
    dataset: Option[String] = None,
    path: Option[String] = None,
    sheetName: Option[String] = None,
    col: Option[Int] = None,
    row: Option[Int] = None,
    cell: Option[String] = None) extends Issue(
    message,
    dataset,
    path,
    sheetName,
    col,
    row,
    cell)

  case class Warn(
    message: String,
    dataset: Option[String] = None,
    path: Option[String] = None,
    sheetName: Option[String] = None,
    col: Option[Int] = None,
    row: Option[Int] = None,
    cell: Option[String] = None) extends Issue(
    message,
    dataset,
    path,
    sheetName,
    col,
    row,
    cell)
}