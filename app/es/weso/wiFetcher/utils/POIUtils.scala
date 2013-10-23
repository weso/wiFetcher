package es.weso.wiFetcher.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.formula.eval.NotImplementedException
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

object POIUtils {

  val EmptyString = ""

  /**
   * This function extract the content from a given cell as a string, using
   * a FormulaEvaluator
   */
  def extractCellValue(cell: Cell, evaluator: FormulaEvaluator)
  (implicit sFetcher: SpreadsheetsFetcher): String = {
    if (cell != null) {
      try {
        val cellValue: CellValue = evaluator.evaluate(cell)
        if (cellValue != null) {
          cellValue.getCellType() match {
            case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cellValue.getBooleanValue).trim
            case Cell.CELL_TYPE_NUMERIC => String.valueOf(cellValue.getNumberValue).trim
            case Cell.CELL_TYPE_STRING => cellValue.getStringValue.trim
            case Cell.CELL_TYPE_BLANK => EmptyString
            case Cell.CELL_TYPE_ERROR => EmptyString
          }
        } else EmptyString
      } catch {
        case e: IllegalArgumentException =>
          sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
            +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
            col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
            `cell` = Some(cell.toString))
          EmptyString
        case e: NotImplementedException =>
          sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
            +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
            col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
            `cell` = Some(cell.toString))
          EmptyString
        case e: RuntimeException =>
          sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
            +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
            col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
            `cell` = Some(cell.toString))
          EmptyString
      }
    } else EmptyString
  }

  /**
   * This method extract from a given a cell, its content as a number. In case
   * that the content not be a number, its return "-1"
   */
  def extractNumericCellValue(cell: Cell, evaluator: FormulaEvaluator)
  (implicit sFetcher: SpreadsheetsFetcher): Option[Double] = {
    try {
      val cellValue: CellValue = evaluator.evaluate(cell)
      if (cellValue != null) {
        cellValue.getCellType match {
          case Cell.CELL_TYPE_NUMERIC => Some(cellValue.getNumberValue)
          case Cell.CELL_TYPE_STRING => {
            cellValue.getStringValue match {
              case e if e.isEmpty => None
              case ".." | "..." | "N/A" => None
              case s => if (s.forall(_.isDigit)) Some(s.toDouble) else None
            }
          }
          case Cell.CELL_TYPE_BLANK => None
          case Cell.CELL_TYPE_ERROR => None
        }
      } else {
        None
      }
    } catch {
      case e: IllegalArgumentException =>
        sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
          +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
          col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
          `cell` = Some(cell.toString))
        None
      case e: NotImplementedException => 
        sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
          +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
          col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
          `cell` = Some(cell.toString))
        None
      case e: RuntimeException =>
          sFetcher.issueManager.addError(message = "Some errors detected within the formula: "
          +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
          col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
          `cell` = Some(cell.toString))
        None
    }
  }

}