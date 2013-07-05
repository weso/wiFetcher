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
import es.weso.wiFetcher.entities.IndicatorType
import es.weso.wiFetcher.entities.IndicatorHighLow
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

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
      //TODO Extract all information and create the Indicator object
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
      createIndicator(id, subindex, component, name, description, source, 
          provider, typ, weight, hl)
    }
  }
  
  def createIndicator(id : String, subindex : String, component : String, 
      name : String, description : String, source : String, provider : String, 
      typ : String, weight : String, hl : String) {
    val indicator = new Indicator
    indicator.id = id
    indicator.label = name
    indicator.comment = description
    indicator.source = source
    indicator.indicatorType = typ match {
      case "Primary" => {IndicatorType.Primary }
      case "Secondary" => {IndicatorType.Secondary }
      case _ => throw new IllegalArgumentException("Indicator type " + typ + 
          " is unknown" )
    }
    indicator.highLow = hl match {
      case "High" => IndicatorHighLow.High
      case "Low" => IndicatorHighLow.Low
      case _ => throw new IllegalArgumentException("Incorrect value for indicator property High/Low")
    }
    indicator.weight = weight.toDouble
    if(indicator.indicatorType.equals(IndicatorType.Primary))
      primaryIndicators += indicator
    else
      secondaryIndicators += indicator  
    val componentObj = SpreadsheetsFetcher.obtainComponent(component)
    indicator.component = componentObj
    componentObj.addIndicator(indicator)
  }
  
  
  
  def getPrimaryIndicators() : List[Indicator] = {
    primaryIndicators.toList
  }
  
  def getSecondaryIndicators() : List[Indicator] = {
    secondaryIndicators.toList
  }

}