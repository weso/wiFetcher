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
 * about the observations of the secondary indicators of the Web Index
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
      //This loop obtains a list of lists with all observations of the secondary
      //indicators
      val obs = for {
        dataset <- datasets
        sheet = obtainSheet(workbook, dataset.id)
      } yield {
        println(dataset.id)
        if (!sheet.isDefined) 
          List.empty
        else parseData(workbook, sheet.get)
      }
      //Combine all lists in only one list
      observations ++= obs.foldLeft(ListBuffer[Observation]())((a, b) => a ++= b)
      logger.info("Finish observations extraction")
    }
  }
  
  /**
   * This auxiliary method obtain a determine sheet
   */
  protected def obtainSheet(workbook : Workbook, datasetId : String) : Option[Sheet] = {
    val auxSheet = workbook.getSheet(datasetId)
    if(auxSheet == null) {
      val sheet = workbook.getSheet(datasetId + " (1)")
      if(sheet == null) None else Some(sheet)
    } else {
      Some(auxSheet)
    }
  }
  
  /**
   * This method checks if there are observations for an indicator that are no 
   * loaded 
   */
  protected def checkSheets(workbook : Workbook) = {
    val sheets = workbook.getNumberOfSheets
    for{
        index <- 0 until sheets        
        sheet = workbook.getSheetAt(index)
        name = sheet.getSheetName
        if(name.contains("-Ordered") || name.contains("-Imputed"))
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

  /**
   * This method load all observations of a sheet
   */
  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Observation] = {
    SecondaryObservationDAOImpl.logger.info("Begin observations extraction")

    //Obtain the initial cell of observation from properties file
    val initialCell = new CellRef(Configuration.getInitialCellSecondaryObservation)
    val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
    val datasetId = if(!sheet.getSheetName.contains("(")){
      sheet.getSheetName 
    } else {
      sheet.getSheetName().substring(0, sheet.getSheetName.indexOf("("))
    }
    val dataset = sFetcher.getDatasetById(datasetId.trim)
    val indicator = sFetcher.obtainIndicatorById(dataset.id.substring(0, dataset.id.lastIndexOf('-')))
    val status = dataset.id.substring(dataset.id.lastIndexOf('-') + 1)
    var countries : Int = 0
    val years : scala.collection.mutable.Set[Int] = scala.collection.mutable.Set.empty
    val observations = for {
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
        } else countries+=1
       
        ret
      }
      
      //We have to iterate throw the excel file
      col <- initialCell.getCol() to sheet.getRow(0).getLastCellNum() - 1
      year = POIUtils.extractNumericCellValue(sheet.getRow(initialCell.getRow() - 1)
        .getCell(col), evaluator)
      if (!year.isEmpty)
    } yield {
      years += year.get.toInt
      val value = POIUtils.extractNumericCellValue(actualRow.getCell(col), evaluator)
      //Create the observation with the extracted data
      logger.info("Extracted observation of: " + dataset.id + " " +
        country.get.iso3Code + " " + year + " " + indicator.get.id + " " + value)
      val label : String = "" + indicator.get.id + " " + status + " in " + country.get.iso3Code + " during " + year.get.toInt
      createObservation(dataset, label, country.get, null,
        indicator.get, year.get, value, status, XslxFile)
    }
    indicator.get.countriesCoverage = countries
    indicator.get.intervalStarts = years.min
    indicator.get.interfalFinishes = years.max
    observations
  }

  def getObservations(): List[Observation] = observations.toList

}

object SecondaryObservationDAOImpl {

  private val logger: Logger = Logger.getLogger(this.getClass)

  private val XslxFile = Some("Observations File")

}