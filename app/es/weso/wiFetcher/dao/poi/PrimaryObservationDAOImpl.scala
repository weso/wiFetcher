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

class PrimaryObservationDAOImpl (
  is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends ObservationDAO with PoiDAO[Observation]{

  import PrimaryObservationDAOImpl._
  
  private val observations: ListBuffer[Observation] = ListBuffer.empty
  
  load(is)
  
  protected def load(is: InputStream) {
    logger.info("Begin primary observations extraction")
    val workbook: Workbook = WorkbookFactory.create(is)
    for(sheetName <- sheets) {
      val sheet = workbook.getSheet(sheetName)
      if (sheet == null) {
        sFetcher.issueManager.addError(
          message = s"The sheet ${sheetName} is not included in the file, so" +
        	  	"it is not possible to load corresponding observations.",
          path = XslxFile)
      } else {
        observations ++= parseData(workbook, sheet)
      }
    }
    logger.info("Finish primary observations extraction")
  }
  
   protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
     val initialCell = new CellRef(Configuration.getInicialCellPrimaryIndicator)
     val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
     val indicatorRow = Configuration.getPrimaryIndicatorRow
     val sheetName = sheet.getSheetName
     val status = sheetName.substring(sheetName.indexOf("-") + 1, sheetName.length)
       
     val indicators = extractIndicators(indicatorRow, initialCell.getCol, 
         sheet, evaluator)
         
     indicators.foreach(indicator => {
    	 sFetcher.datasets += Dataset(indicator.id + "-" + status)
     })
     
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
       if(!indicator.isEmpty)       
     }yield {
       val dataset = sFetcher.getDatasetById(indicatorId + "-" + status)
       val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
       logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indicator.get.id + " " + value)
       val label : String = "" + indicator.get.id + " in " + country.get.iso3Code + " during " 
    		  + year.toInt
       createObservation(dataset, label, country.get, null, indicator.get, year, 
           value, status, XslxFile)
     }
   }
   
   private def extractIndicators(rowIndicators : Int, initialCol : Int, 
       sheet : Sheet, evaluator : FormulaEvaluator) : Seq[Indicator] = {
     val row = sheet.getRow(rowIndicators)
     for {
       col <- initialCol to row.getLastCellNum
       indicatorId = POIUtils.extractCellValue(row.getCell(col), evaluator)
       if(!indicatorId.isEmpty)
       indicator = sFetcher.obtainIndicatorById(indicatorId)
       if{
         val ret = indicator.isDefined
         if(ret == false) {
           sFetcher.issueManager.addError(message = new StringBuilder("Indicator ")
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
  
    private val sheets : Array[String] = Array("Survey-Raw", "Survey-Ordered", 
      "Survey-Normalised")

  private val XslxFile = Some("Observations File")

}