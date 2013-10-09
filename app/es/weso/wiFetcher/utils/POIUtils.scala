package es.weso.wiFetcher.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.formula.eval.NotImplementedException

object POIUtils {

  val EmptyString = ""

  /**
   * This function extract the content from a given cell as a string, using
   * a FormulaEvaluator
   */
  def extractCellValue(cell: Cell, evaluator: FormulaEvaluator): String = {
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
          IssueManagerUtils.addError(message = "Some errors detected within the formula"
            +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
            col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
            `cell` = Some(cell.toString))
          EmptyString
        case e: NotImplementedException =>
          IssueManagerUtils.addError(message = "Some errors detected within the formula"
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
  def extractNumericCellValue(cell: Cell, evaluator: FormulaEvaluator): Double = {
    try {
      val cellValue: CellValue = evaluator.evaluate(cell)
      if (cellValue != null) {
        cellValue.getCellType match {
          case Cell.CELL_TYPE_NUMERIC => cellValue.getNumberValue
          case Cell.CELL_TYPE_STRING => {
            cellValue.getStringValue match {
              case e if e.isEmpty => -1
              case ".." | "..." | "N/A" => -1
              case s => if (s.forall(_.isDigit)) s.toDouble else -1
            }
          }
          case Cell.CELL_TYPE_BLANK => -1
          case Cell.CELL_TYPE_ERROR => -1
        }
      } else {
        -1
      }
    } catch {
      case e: IllegalArgumentException =>
        IssueManagerUtils.addError(message = "Some errors detected within the formula: "
          +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
          col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
          `cell` = Some(cell.toString))
        -1
      case e: NotImplementedException => 
        IssueManagerUtils.addError(message = "Some errors detected within the formula: "
          +e.getMessage, sheetName = Some(cell.getSheet.getSheetName),
          col = Some(cell.getColumnIndex), row = Some(cell.getRowIndex),
          `cell` = Some(cell.toString))
        -1
    }
  }

}