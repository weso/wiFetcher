package es.weso.wiFetcher.dao.poi

import java.io.InputStream

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

import org.apache.log4j.Logger
import org.apache.poi.hssf.util.{ CellReference => CellRef }
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.ObservationDAO
import es.weso.wiFetcher.entities.Area
import es.weso.wiFetcher.entities.Computation
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.ObservationStatus
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.utils.IssueManagerUtils

/**
 * This class contains the implementation that allows extract all information
 * about the observations of the Web Index
 *
 * This implementation is temporary because Web Foundation or TAS still has not
 * give us the final structure of the sheet that contains the observations.
 * Maybe the implementation has to change.
 *
 */
class SecondaryObservationDAOImpl(
  is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends ObservationDAO with PoiDAO[Observation] {

  import SecondaryObservationDAOImpl._

  private val observations: ListBuffer[Observation] = ListBuffer.empty

  load(is)

  protected def load(is: InputStream) {

    val datasets = sFetcher.getDatasets

    if (datasets == null) {
      sFetcher.issueManager.addError(
        message = "The datasets are not loaded. It is mandatory to load " +
          "the datasets in order to process the Observations",
        path = XslxFile)
    } else {

      logger.info("Begin observations extraction")
      val workbook: Workbook = WorkbookFactory.create(is)
      checkSheets(workbook)
      val obs = for {
        dataset <- datasets
        sheet = workbook.getSheet(dataset.id)
      } yield {
        println(dataset.id)
        if (sheet == null) {
          sFetcher.issueManager.addError(
            message = s"The dataset ${dataset.id} are invalid or empty. It is " + 
        	  "mandatory to have data " +
              "within the datasets in order to process the Observations",
            path = XslxFile)
          List.empty
        } else parseData(workbook, sheet)
      }
      observations ++= obs.foldLeft(ListBuffer[Observation]())((a, b) => a ++= b)
      logger.info("Finish observations extraction")
    }
  }
  
  protected def checkSheets(workbook : Workbook) = {
    val sheets = workbook.getNumberOfSheets
    for{
        index <- 0 until sheets        
        sheet = workbook.getSheetAt(index)
        name = sheet.getSheetName
        if(name.contains("-Ordered") || name.contains("-Imputed") || 
          name.contains("-Normalised"))
        indicatorId = name.substring(0, name.indexOf("-"))
        if(!indicatorId.equals("Survey"))
        if(!sFetcher.obtainIndicatorById(indicatorId).isDefined)
    } { 
      sFetcher.issueManager.addWarn(
            message = s"There are observations for dataset ${name}, but indicator ${indicatorId} " +
              "it's no present in structure file",
            path = XslxFile)
    }
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
    SecondaryObservationDAOImpl.logger.info("Begin observations extraction")

    //Obtain the initial cell of observation from properties file
    val initialCell = new CellRef(Configuration.getInitialCellSecondaryObservation)
    val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
    val dataset = sFetcher.getDatasetById(sheet.getSheetName())
    val indicator = sFetcher.obtainIndicatorById(dataset.id.substring(0, dataset.id.lastIndexOf('-'))) /*obtainIndicator(sheet, Configuration.getIndicatorCell, evaluator)*/
    val status = dataset.id.substring(dataset.id.lastIndexOf('-') + 1)

    for {
      row <- initialCell.getRow() to sheet.getLastRowNum()
      actualRow = sheet.getRow(row)
      if actualRow != null
      if !POIUtils.extractCellValue(actualRow.getCell(0), evaluator).trim.isEmpty
      countryName = POIUtils.extractCellValue(actualRow.getCell(0), evaluator)
      //Obtain the country corresponds to the observation 
      country = obtainCountry(countryName)
      //If country of the observation is null, there is no observation
      //for this cell
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
      //We have to iterate throw the excel file
      col <- initialCell.getCol() to sheet.getRow(0).getLastCellNum() - 1
      year = POIUtils.extractNumericCellValue(sheet.getRow(initialCell.getRow() - 1)
        .getCell(col), evaluator)
      if (!year.isEmpty)
    } yield {
      val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
      //Create the observation with the extracted data
      logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indicator.get.id + " " + value)
      val label : String = "" + indicator.get.id + " in " + country.get.iso3Code + " during " + year.get.toInt
      createObservation(dataset, label, country.get, null,
        indicator.get, year.get, value, status, XslxFile)
    }
  }

  /**
   * This method has to obtain the indicator corresponds to an observation
   * @param sheet The sheet that contains all observation of a dataset
   * @param dataset A dataset corresponds to the excel sheet from we have to
   * extract the indicator
   * @param column The column corresponds to an observation
   * @param row The row corresponds to an observation
   * @param initialCell The initial cell of the observations
   * @return An indicator
   */
  /*def obtainIndicator(sheet: Sheet, cell: String, evaluator: FormulaEvaluator): Indicator = {
    val cellReference = new CellRef(cell)
    val indicatorName = POIUtils.extractCellValue(
      sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), evaluator)
    sFetcher.obtainIndicator(indicatorName)
  }*/

  //
  //  /**
  //   * This method has to extract the status of the observations
  //   * @param cell The cell where is the status
  //   * @param sheet The sheet that contains all observations of a dataset
  //   * @return The values extracted of the status cell
  //   */
  //  def obtainStatus(cell: String, sheet: Sheet, evaluator: FormulaEvaluator): String = {
  //    val cellReference = new CellRef(cell)
  //    val stat = sheet.getRow(cellReference.getRow()).getCell(
  //      cellReference.getCol())
  //    if (stat == null)
  //      throw new IllegalArgumentException("Status cell is empty")
  //    POIUtils.extractCellValue(stat, evaluator)
  //  }

  def getObservations(): List[Observation] = observations.toList

}

object SecondaryObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass)

  private val XslxFile = Some("Observations File")

}