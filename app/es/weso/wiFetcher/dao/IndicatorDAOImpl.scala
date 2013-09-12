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
import java.io.InputStream
import org.apache.poi.ss.usermodel.Workbook
import java.io.PushbackInputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.POIXMLDocument
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.log4j.Logger

/**
 * This class contains the implementation that allows to load all information
 * about Web Index indicators
 * 
 * At the moment, this class extracts the information of the indicators from an
 * excel file that follows the structure of 2012 Web Index. Maybe we have 
 * to change the implementation
 */
class IndicatorDAOImpl(is : InputStream)    
	extends IndicatorDAO {
  
  /**
   * The name of the sheet that contains all indicators data
   */
  private val SHEET_NAME : String = "Indicators"
    
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  /**
   *  A list with all primary indicators of the Web Index
   */  
  private var primaryIndicators : ListBuffer[Indicator] = 
    new ListBuffer[Indicator]()
    
  /**
   * A list with all secondary indicators of the Web Index
   */
  private var secondaryIndicators : ListBuffer[Indicator] = 
    new ListBuffer[Indicator]()
    
  load(is)  
  
  def load(is : InputStream) {
    val workbook = WorkbookFactory.create(is)
    parseData(workbook, extractSheet(null, workbook))
  }
  
  /**
   * This method has to create an indicator from the parameters that receive
   * @param id The identifier of the indicator
   * @param subindex The subindex identifier that owns the indicator
   * @param component The component identifier that owns the indicator
   * @param name The complete name of the indicator
   * @param description A complete description of the indicator
   * @param source The source of the indicator
   * @param provider The entity or organization that provides indicator 
   * information
   * @param typ The type of the indicator (primary or secondary)
   * @param weigth The weigth that has the indicator in order to calculate
   * the Web Index
   * @param hl A variable that indicates if for an indicator, values high or 
   * low are preferred
   */
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
    //Ask to SpreadsheetsFetcher for the component that owns the indicator
    val componentObj = SpreadsheetsFetcher.obtainComponent(component)
    indicator.component = componentObj
    componentObj.addIndicator(indicator)
  }
  
  /**
   * This method returns a list with all primary indicators
   */
  def getPrimaryIndicators() : List[Indicator] = {
    primaryIndicators.toList
  }
  
  /**
   * This method returns a list with all secondary indicators
   */
  def getSecondaryIndicators() : List[Indicator] = {
    secondaryIndicators.toList
  }
  
  private def parseData(workbook: Workbook, sheet : Sheet): Unit = {
	  
	  //Obtain the initial cell to extract the information from properties files
	  val cellReference = new CellReference(
	      Configuration.getInitialCellIndicatorsSheet)
	  //For each row, create a new indicator and extract all information
	  logger.info("Begin indicators extraction")
	  for(row <- cellReference.getRow() to sheet.getLastRowNum()) {
	    val evaluator : FormulaEvaluator = 
	      workbook.getCreationHelper().createFormulaEvaluator()
	    val actualRow = sheet.getRow(row)
	    //In the properties file, we define the number of the columns that 
	    //contains each indicator property
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
	  logger.info("Finish indicators extraction")
	}
  
  private def extractSheet(path: Option[String], workbook:Workbook): Sheet = {
	  //Obtain the sheet corresponding with indicators
	  val sheet : Sheet = workbook.getSheet(SHEET_NAME)
	  if(sheet == null) {
	    logger.error("Not exist a sheet in the file " + 
	        path.get + " with the name " + SHEET_NAME)
	    throw new IllegalArgumentException("Not exist a sheet in the file " + 
	        path.get + " with the name " + SHEET_NAME)
	  }
	  sheet
	}
  
}