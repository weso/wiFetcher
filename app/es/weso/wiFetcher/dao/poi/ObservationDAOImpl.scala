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
import es.weso.wiFetcher.entities.issues._
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
class ObservationDAOImpl(
  is: InputStream) extends ObservationDAO with PoiDAO[Observation] {

  import ObservationDAOImpl._

  private val observations: ListBuffer[Observation] = ListBuffer.empty

  load(is)

  protected def load(is: InputStream) {

    val datasets = SpreadsheetsFetcher.getDatasets

    if (datasets == null) {
      IssueManagerUtils.addError(
        message = "The datasets are not loaded. It is mandatory to load " +
          "the datasets in order to process the Observations",
        path = XslxFile)
    } else {

      logger.info("Begin observations extraction")
      val workbook: Workbook = WorkbookFactory.create(is)
      val obs = for {
        dataset <- datasets
        sheet = workbook.getSheet(dataset.id)
      } yield {
        println(dataset.id)
        if (sheet == null) {
          IssueManagerUtils.addError(
            message = s"The dataset ${dataset.id} are invalid or empty. It is mandatory to have data " +
              "within the datasets in order to process the Observations",
            path = XslxFile)
          List.empty
        } else parseData(workbook, sheet)
      }
      observations ++= obs.foldLeft(ListBuffer[Observation]())((a, b) => a ++= b)
      logger.info("Finish observations extraction")
    }
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
    ObservationDAOImpl.logger.info("Begin observations extraction")

    //Obtain the initial cell of observation from properties file
    val initialCell = new CellRef(Configuration.getInitialCellSecondaryObservation)
    val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
    val dataset = SpreadsheetsFetcher.getDatasetById(sheet.getSheetName())
    val indicator = SpreadsheetsFetcher.obtainIndicatorById(dataset.id.substring(0, dataset.id.lastIndexOf('-')))/*obtainIndicator(sheet, Configuration.getIndicatorCell, evaluator)*/
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
          IssueManagerUtils.addError(message = s"Country ${countryName} not definend", path = XslxFile,
            sheetName = Some(sheet.getSheetName), col = Some(0), `row` = Some(row),
            cell = Some(countryName))
        }
        ret
      }
      //We have to iterate throw the excel file
      col <- initialCell.getCol() to sheet.getRow(initialCell.getRow()).getLastCellNum() - 1
      year = POIUtils.extractCellValue(sheet.getRow(initialCell.getRow() - 1)
        .getCell(col), evaluator)
      if(!year.isEmpty())
    } yield {
      /*val year = POIUtils.extractCellValue(sheet.getRow(initialCell.getRow() - 1)
        .getCell(col), evaluator)*/
      val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
      //Create the observation with the extracted data
      logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indicator.id + " " + value)
      createObservation(dataset, "", country.get, null,
        indicator, year.toDouble, value, status)
    }
  }

  /**
   * This method has to extract the name of the country corresponding to the
   * observation.
   * @param sheet The sheet that contains all observation of a dataset
   * @param dataset A dataset corresponds to the excel sheet from we have to
   * extract the country name
   * @param column The column corresponds to an observation
   * @param row The row corresponds to an observation
   * @param initialCell The initial cell of the observations
   * @return A country corresponds to an observations
   */
  def obtainCountry(countryName: String): Option[Country] = {
    logger.info("Obtaining country with name: " + countryName)
    //Ask to SpreadsheetFetcher for the country accord to the Web Index name
    val country = SpreadsheetsFetcher.obtainCountry(countryName)
    country match {
      case Some(c) => ""
      case None => "foo"
    }
    country
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
  def obtainIndicator(sheet: Sheet, cell: String, evaluator: FormulaEvaluator): Indicator = {
    val cellReference = new CellRef(cell)
    val indicatorName = POIUtils.extractCellValue(
      sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), evaluator)
    SpreadsheetsFetcher.obtainIndicator(indicatorName)
  }

  /**
   * This method has to create an observation
   * @param dataset The dataset corresponds to an observation
   * @param label
   * @param area The area that refers the observation
   * @param computation
   * @param indicator The indicator measured at the observations
   * @param year The year of the observation
   * @param value The value of the observation
   * @param status The status of the observation
   */
  def createObservation(dataset: Dataset, label: String, area: Area,
    computation: Computation, indicator: Indicator, year: Double,
    value: Double, status: String): Observation = {

    val tmpStatus = if (value == -1)
      ObservationStatus.Missed
    else status match {
      case "Raw" => ObservationStatus.Raw
      case "Imputed" => ObservationStatus.Imputed
      case "Normalised" => ObservationStatus.Normalised
      case "Missed" => ObservationStatus.Missed
      case "Sorted" => ObservationStatus.Sorted
      case "Adjusted" => ObservationStatus.Adjusted
      case "Weighted" => ObservationStatus.Weighted
      case "Ordered" => ObservationStatus.Ordered
      case _ =>
        IssueManagerUtils.addError(message = "Observation status " +
          status + " is unknown", path = XslxFile)
        ObservationStatus.Wrong
    }

    Observation(
      dataset,
      label,
      area,
      computation,
      indicator,
      year.toInt,
      value,
      tmpStatus)

  }
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

object ObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass)

  private val XslxFile = Some("Observations File")

}