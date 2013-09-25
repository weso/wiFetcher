package es.weso.wiFetcher.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.Sheet

object POIUtils {

  /**
   * This function extract the content from a given cell as a string, using
   * a FormulaEvaluator
   */
  def extractCellValue(cell: Cell, evaluator: FormulaEvaluator): String = {
    if (cell != null) {
      val cellValue: CellValue = evaluator.evaluate(cell)
      if (cellValue != null) {
        cellValue.getCellType() match {
          case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cellValue.getBooleanValue).trim
          case Cell.CELL_TYPE_NUMERIC => String.valueOf(cellValue.getNumberValue).trim
          case Cell.CELL_TYPE_STRING => cellValue.getStringValue.trim
          case Cell.CELL_TYPE_BLANK => ""
          case Cell.CELL_TYPE_ERROR => ""
        }
      } else {
        ""
      }
    } else {
      ""
    }
  }

  /**
   * This method extract from a given a cell, its content as a number. In case
   * that the content not be a number, its return "-1"
   */
  def extractNumericCellValue(cell: Cell, evaluator: FormulaEvaluator): Double = {
    val cellValue: CellValue = evaluator.evaluate(cell)
    if (cellValue != null) {
      cellValue.getCellType() match {
        case Cell.CELL_TYPE_NUMERIC => cellValue.getNumberValue
        case Cell.CELL_TYPE_STRING => {
          cellValue.getStringValue() match {
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
  }

}