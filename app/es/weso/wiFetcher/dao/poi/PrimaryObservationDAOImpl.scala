package es.weso.wiFetcher.dao.poi

import java.io.InputStream
import scala.collection.mutable.ListBuffer
import org.apache.log4j.Logger
import org.apache.poi.hssf.util.{CellReference => CellRef}
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.ObservationDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.POIUtils
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Cell
import es.weso.wiFetcher.entities.IndicatorType

/**
 * This class contains the implementation that allows extract all information
 * about the observations of the primary indicators of the Web Index.
 * 
 * These observations are defined in sheets that follows a regular expression
 * given by the Web Foundation
 *
 */
class PrimaryObservationDAOImpl (
  is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends ObservationDAO with PoiDAO[Observation]{

  import PrimaryObservationDAOImpl._
  
  private val observations: ListBuffer[Observation] = ListBuffer.empty
  
  load(is)
  
  protected def load(is: InputStream) {
    logger.info("Begin primary observations extraction")
    val workbook: Workbook = WorkbookFactory.create(is)
    val sheets : ListBuffer[String] = ListBuffer.empty
    
    //First, we search all sheets which name satisfies regular expression 
    for(index <- 0 until workbook.getNumberOfSheets()) {
      val sheetName = workbook.getSheetAt(index).getSheetName()
      if(pattern.findFirstMatchIn(sheetName).isDefined) 
        sheets += sheetName
    }
    
    //Process each sheet found 
    for(sheetName <- sheets) {
      val sheet = workbook.getSheet(sheetName)
      if (sheet == null) {
        sFetcher.issueManager.addError(
          message = s"The sheet ${sheetName} is not included in the file, so " +
        	  	"it is not possible to load corresponding observations.",
          path = XslxFile)
      } else {
        observations ++= parseData(workbook, sheet)
      }
    }
    logger.info("Finish primary observations extraction")
  }
  
  /**
   * This auxiliary method has to determine the initial cell of the observations
   * in order to avoid empty rows at the beginning of the file
   */
  protected def obtainInitialCell(cell : CellRef, sheet : Sheet) : CellRef = {
    if(cell != null) {
      cell
    } else {
      val nextRowCell : Cell = sheet.getRow(cell.getRow + 1).getCell(cell.getCol())
      obtainInitialCell(new CellRef(nextRowCell.getRowIndex, 
          nextRowCell.getColumnIndex()), sheet)
    }
  }
  
  /**
   * This method load all observations from a sheet
   */
   protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
     val initialCell = obtainInitialCell(
         new CellRef(Configuration.getInicialCellPrimaryIndicator), sheet)
     val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
     val indicatorRow = Configuration.getPrimaryIndicatorRow
     val sheetName = sheet.getSheetName
     val status = sheetName.substring(sheetName.indexOf("-") + 1, sheetName.length)
       
     //Extract all indicators that have observations in the sheet
     val indicators = extractIndicators(indicatorRow, initialCell.getCol, 
         sheet, evaluator)
         
     //For each indicator create the corresponding dataset    
     indicators.foreach(indicator => {
       if(indicator.indicatorType.equals(IndicatorType.Primary)) 
    	 sFetcher.datasets += Dataset(indicator.id + "-" + status)
     })
     
     //Main loop that loads all information
     for{
       row <- initialCell.getRow to sheet.getLastRowNum
       actualRow = sheet.getRow(row)
       if(actualRow != null)
       if !POIUtils.extractCellValue(actualRow.getCell(0), evaluator).trim.isEmpty
       countryName = POIUtils.extractCellValue(actualRow.getCell(0), evaluator)
       //Obtain the country corresponds to the observation 
       country = obtainCountry(countryName)
       year : Double = 2013
       if {
        val ret = country.isDefined
        if (ret == false) {
          sFetcher.issueManager.addError(message = new StringBuilder("Country ")
            .append(countryName).append(" is not defined").toString, path = XslxFile,
            sheetName = Some(sheet.getSheetName), col = Some(0), `row` = Some(row),
            cell = Some(countryName))
        }
        ret
       }
       col <- initialCell.getCol to sheet.getRow(indicatorRow).getLastCellNum
       cell = sheet.getRow(indicatorRow).getCell(col)
       indicatorId = POIUtils.extractCellValue(cell, evaluator)
       if(!indicatorId.isEmpty())
       indicator = indicators.find(indicator => indicator.id.equals(indicatorId))
       if(indicator.isDefined)       
     }yield {
       val dataset = sFetcher.getDatasetById(indicatorId + "-" + status)
       val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
       logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indicator.get.id + " " + value)
       val label : String = "" + indicator.get.id + " " + status + " in " + country.get.iso3Code + " during " + year.toInt
       createObservation(dataset, label, country.get, null, indicator.get, year, 
           value, status, XslxFile)
     }
   }
   
   /**
    * This method extract all indicators that have observations in this sheet
    */
   private def extractIndicators(rowIndicators : Int, initialCol : Int, 
       sheet : Sheet, evaluator : FormulaEvaluator) : Seq[Indicator] = {
     val row = sheet.getRow(rowIndicators)
     for {
       col <- initialCol to row.getLastCellNum
       //Extract id of an indicator
       indicatorId = POIUtils.extractCellValue(row.getCell(col), evaluator)
       if(!indicatorId.isEmpty)
       //Obtain an indicator given it's id
       indicator = sFetcher.obtainIndicatorById(indicatorId)
       if{
         val ret = indicator.isDefined
         if(ret == false) {
           sFetcher.issueManager.addWarn(message = new StringBuilder("Indicator ")
            .append(indicatorId).append(" is not defined").toString, path = XslxFile,
            sheetName = Some(sheet.getSheetName), col = Some(0), `row` = Some(rowIndicators),
            cell = Some(indicatorId))
         }
         ret
       }
     } yield {
       indicator.get
     }
   }
  
  def getObservations(): List[Observation] = observations.toList
  
}

object PrimaryObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass)
  
  //Regular expression for the sheet names
  val pattern = """(?i)(odb|gi|survey)-(?i)(ordered)""".r

  private val XslxFile = Some("Observations File")

}