package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import es.weso.wiFetcher.entities.Indicator
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.utils.FileUtils
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.File
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.hssf.util.CellReference
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.utils.POIUtils
import org.apache.poi.ss.usermodel.FormulaEvaluator

class IndicatorDAOImpl(path : String, relativePath : Boolean) 
	extends IndicatorDAO {
  
  private val SHEET_NAME : String = "Indicators"
  
  private var primaryIndicators : ListBuffer[Indicator] = 
    new ListBuffer[Indicator]()
  private var secondaryIndicators : ListBuffer[Indicator] = 
    new ListBuffer[Indicator]()
    
  load(FileUtils.getFilePath(path, relativePath))  
  
  def load(path : String) {
    val workbook = WorkbookFactory.create(new FileInputStream(new File(path)))
    val sheet : Sheet = workbook.getSheet(SHEET_NAME)
    if(sheet == null) 
      throw new IllegalArgumentException("Not exist a sheet in the file " + 
          path + " with the name " + SHEET_NAME)
    val cellReference = new CellReference(
        Configuration.getInitialCellIndicatorsSheet)
    for(row <- cellReference.getRow() to sheet.getLastRowNum()) {
      val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
      val actualRow = sheet.getRow(row)
      //TODO Extract all information
      val id = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorIdColumn))
      val subindex = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorSubindexColumn))
      val component = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorComponentColumn))
      val name = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorNameColumn))
      val description = POIUtils.extractCellValue(actualRow.getCell(
    	  Configuration.getIndicatorDescriptionColumn))
	  val source = POIUtils.extractCellValue(actualRow.getCell(
	      Configuration.getIndicatorSourceColumn))
      val provider = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorProviderColumn))
      val typ = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorTypeColumn))
      val weight = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorWeightColumn), evaluator)
      val hl = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorHLColumn))  
      println(id + " " + subindex + " " + component + " " + name + " " + 
          description + " " + source + " " + provider + " " + typ + " " +
          weight + " " + hl)
    }
  }
  
  def getPrimaryIndicators() : List[Indicator] = {
    primaryIndicators.toList
  }
  
  def getSecondaryIndicators() : List[Indicator] = {
    secondaryIndicators.toList
  }

}