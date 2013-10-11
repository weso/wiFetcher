package es.weso.wiFetcher.dao.poi

import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.dao.ObservationDAO
import java.io.InputStream
import org.apache.log4j.Logger
import scala.collection.mutable.ListBuffer
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.hssf.util.{ CellReference => CellRef }
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.entities.Dataset

class PrimaryObservationDAOImpl (
  is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends ObservationDAO with PoiDAO[Observation]{

  import PrimaryObservationDAOImpl._
  
  private val observations: ListBuffer[Observation] = ListBuffer.empty
  
  load(is)
  
  protected def load(is: InputStream) {
    logger.info("Begin primary observations extraction")
    val workbook: Workbook = WorkbookFactory.create(is)
    val sheet = workbook.getSheet(sheetName)
    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = s"The sheet ${sheetName} is not included in the file. " +
        		"Primary observations cannot be loaded.",
        path = XslxFile)
      List.empty
    } else parseData(workbook, sheet)
      logger.info("Finish primary observations extraction")
  }
  
   protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
     val initialCell = new CellRef(Configuration.getInicialCellPrimaryIndicator)
     val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
     val indicatorRow = Configuration.getPrimaryIndicatorRow
     val status = "Raw"
     val lastCol = Configuration.getPrimaryIndicatorLastCol  
     
     for{
       row <- initialCell.getRow to sheet.getLastRowNum
       actualRow = sheet.getRow(row)
       if(actualRow != null)
       if !POIUtils.extractCellValue(actualRow.getCell(0), evaluator).trim.isEmpty
       countryName = POIUtils.extractCellValue(actualRow.getCell(0), evaluator)
       //Obtain the country corresponds to the observation 
       country = obtainCountry(countryName)
       
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
       
       col <- initialCell.getCol to lastCol
       year : Double = 2013
       cell = sheet.getRow(indicatorRow).getCell(col)
       indicator = POIUtils.extractCellValue(cell, evaluator)
       if(!indicator.isEmpty())
     }yield {
       val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
       val dataset = Dataset(indicator + "-Raw")
       sFetcher.datasets += dataset
       val indi = sFetcher.obtainIndicatorById(indicator)
       logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indi.id + " " + value)
       createObservation(dataset, "", country.get, null, indi, year, 
           value, status, XslxFile)
     }
   }
  
  def getObservations(): List[Observation] = observations.toList
  
}

object PrimaryObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass)
  
  private val sheetName : String = "Survey-Raw"

  private val XslxFile = Some("Observations File")

}