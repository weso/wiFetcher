package es.weso.wiFetcher.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.CellValue

object POIUtils {
  
  def extractCellValue(cell : Cell) : String = {
    if(cell != null) {
      cell.getCellType() match {
      	case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cell.getBooleanCellValue).trim
      	case Cell.CELL_TYPE_NUMERIC => String.valueOf(cell.getNumericCellValue).trim
      	case Cell.CELL_TYPE_STRING => cell.getStringCellValue.trim
      	case Cell.CELL_TYPE_BLANK => ""
      	case Cell.CELL_TYPE_ERROR => ""
      }
    }else {
      ""
    }    
  }
  
  def extractCellValue(cell : Cell, evaluator : FormulaEvaluator) : String = {
    val cellValue : CellValue = evaluator.evaluate(cell)
    if(cellValue != null) {
      cellValue.getCellType() match {
        case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cellValue.getBooleanValue).trim
        case Cell.CELL_TYPE_NUMERIC => String.valueOf(cellValue.getNumberValue).trim
        case Cell.CELL_TYPE_STRING => cellValue.getStringValue.trim
        case Cell.CELL_TYPE_BLANK => ""
        case Cell.CELL_TYPE_ERROR => ""
      }
    }else {
     "" 
    }
  }

}