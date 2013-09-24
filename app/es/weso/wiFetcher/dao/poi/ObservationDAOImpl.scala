package es.weso.wiFetcher.dao.poi

import java.io.InputStream

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

import org.apache.log4j.Logger
import org.apache.poi.hssf.util.CellReference
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
      logger.error("List of datasets to load their " +
        "observations is null")
      throw new IllegalArgumentException("List of datasets to load their " +
        "observations is null")
    }

    logger.info("Begin observations extraction")

    val workbook: Workbook = WorkbookFactory.create(is)
    val obs = for {
      dataset <- datasets
      sheet = workbook.getSheet(dataset.id)
    } yield {
      if (sheet == null) {
        ObservationDAOImpl.logger.error("There isn't data for dataset: " +
          dataset.id)
        throw new IllegalArgumentException("There isn't data for dataset: " +
          dataset.id)
      }
      parseData(workbook, sheet)
    }
    observations ++= obs.foldLeft(ListBuffer[Observation]())((a, b) => a ++= b)
    logger.info("Finish observations extraction")
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
    ObservationDAOImpl.logger.info("Begin observations extraction")

    //Obtain the initial cell of observation from properties file
    val initialCell = new CellReference(Configuration.getInitialCellSecondaryObservation)
    val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
    for {
      row <- initialCell.getRow() to sheet.getLastRowNum()
      actualRow = sheet.getRow(row)
      if actualRow != null
      if !POIUtils.extractCellValue(actualRow.getCell(0), evaluator).trim().isEmpty()
      countryName = POIUtils.extractCellValue(actualRow.getCell(0))
      //Obtain the indicator corresponds to the observation
      indicator = obtainIndicator(sheet, Configuration.getIndicatorCell)
      //Obtain the country corresponds to the observation 
      country = obtainCountry(countryName)
      //If country of the observation is null, there is no observation
      //for this cell
      if country != null
      //We have to iterate throw the excel file
      column <- initialCell.getCol() to actualRow.getLastCellNum() - 1
      //TODO Have to extract the year for the spreadsheet
      year = POIUtils.extractCellValue(sheet.getRow(
        initialCell.getRow() - 1).getCell(column), evaluator)
      value = POIUtils.extractNumericCellValue(actualRow.getCell(column), evaluator)
      dataset = SpreadsheetsFetcher.getDatasetById(sheet.getSheetName())
      status = dataset.id.substring(dataset.id.lastIndexOf('-'))
    } yield {
      //Create the observation with the extracted data
      logger.info("Extracted observation of: " + dataset.id + " " +
        country.iso3Code + " " + indicator + " " + value)
      createObservation(dataset, "", country, null,
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
  def obtainCountry(countryName: String): Country = {
    logger.info("Obtaining country with name: " + countryName)
    //Ask to SpreadsheetFetcher for the country accord to the Web Index name
    SpreadsheetsFetcher.obtainCountry(countryName)
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
  def obtainIndicator(sheet: Sheet, cell: String): Indicator = {
    val cellReference = new CellReference(cell)
    val indicatorName = POIUtils.extractCellValue(
      sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))
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
    else {
      status match {
        case "Raw" => ObservationStatus.Raw
        case "Imputed" => ObservationStatus.Imputed
        case "Normalised" => ObservationStatus.Normalised
        case "Missed" => ObservationStatus.Missed
        case "Sorted" => ObservationStatus.Sorted
        case "Adjusted" => ObservationStatus.Adjusted
        case "Weighted" => ObservationStatus.Weighted
        case "Ordered" => ObservationStatus.Ordered
        case _ => throw new IllegalArgumentException("Observation status " +
          status + " is unknown")
      }
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

  /**
   * This method has to extract the status of the observations
   * @param cell The cell where is the status
   * @param sheet The sheet that contains all observations of a dataset
   * @return The values extracted of the status cell
   */
  def obtainStatus(cell: String, sheet: Sheet): String = {
    val cellReference: CellReference = new CellReference(cell)
    val stat = sheet.getRow(cellReference.getRow()).getCell(
      cellReference.getCol())
    if (stat == null)
      throw new IllegalArgumentException("Status cell is empty")
    POIUtils.extractCellValue(stat)
  }

  def getObservations(): List[Observation] = observations.toList
}

object ObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass())

}