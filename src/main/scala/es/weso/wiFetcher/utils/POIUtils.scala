package es.weso.wiFetcher.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.CellValue

object POIUtils {
  
  def extractCellValue(cell : Cell) : String = {
    if(cell != null) {
      cell.getCellType() match {
      	case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cell.getBooleanCellValue)
      	case Cell.CELL_TYPE_NUMERIC => String.valueOf(cell.getNumericCellValue)
      	case Cell.CELL_TYPE_STRING => cell.getStringCellValue
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
        case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cellValue.getBooleanValue)
        case Cell.CELL_TYPE_NUMERIC => String.valueOf(cellValue.getNumberValue)
        case Cell.CELL_TYPE_STRING => cellValue.getStringValue
        case Cell.CELL_TYPE_BLANK => ""
        case Cell.CELL_TYPE_ERROR => ""
      }
    }else {
     "" 
    }
  }

}