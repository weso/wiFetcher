package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import scala.collection.mutable.ListBuffer
import org.apache.poi.hssf.util.CellReference
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.configuration.Configuration
import org.apache.poi.ss.usermodel.FormulaEvaluator
import es.weso.wiFetcher.entities.Area
import es.weso.wiFetcher.entities.Computation
import es.weso.wiFetcher.entities.Indicator
import org.apache.poi.ss.usermodel.Sheet
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.entities.ObservationStatus
import es.weso.wiFetcher.entities.ObservationStatus._
import org.apache.poi.ss.usermodel.Cell
import org.apache.log4j.Logger

/**
 * This class contains the implementation that allows extract all information
 * about the observations of the Web Index
 * 
 * This implementation is temporary because Web Foundation or TAS still has not
 * give us the final structure of the sheet that contains the observations. 
 * Maybe the implementation has to change.
 *  
 */
class ObservationDAOImpl(path : String, relativePath : Boolean) 
	extends ObservationDAO {
  
  /**
   * The excel workbook that contains the information about observations
   */
  var workbook : Workbook = WorkbookFactory.create(new FileInputStream(
      new File(FileUtils.getFilePath(path, relativePath))))
      
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  /**
   *  This method loads all observations for each dataset given in a list
   */
  def getObservations(datasets : List[Dataset]) : List[Observation] = {
    if(datasets == null){
      logger.error("List of datasets to load their " +
      		"observations is null")
      throw new IllegalArgumentException("List of datasets to load their " +
      		"observations is null")
    }
    var observations = ListBuffer[Observation]()
    //For each dataset, has to load all it's observations
    for(dataset <- datasets)
      observations.insertAll(0, getObservationsByDataset(dataset))
    observations.toList
  }
  
  /**
   * Given a dataset, this method has to load all it's observations
   * @param dataset A dataset to extract all it's observations
   * @return A list with all observations of a dataset
   */
  def getObservationsByDataset(dataset : Dataset) : List[Observation] = {
    if(dataset == null) {
      logger.error("Cannot extract observations of a " +
      		"null dataset")
      throw new IllegalArgumentException("Cannot extract observations of a " +
      		"null dataset")
    }
    var observations = ListBuffer[Observation]()
    //Each dataset corresponds to a sheet of the excel file
    val sheet = workbook.getSheet(dataset.id)
    if(sheet == null) {
      logger.error("There isn't data for dataset: " + 
          dataset.id)
      throw new IllegalArgumentException("There isn't data for dataset: " + 
          dataset.id)
    }     
    logger.info("Begin observations extraction")
    //Status of observations depends on excel templates
    val status = obtainStatus(Configuration.getStatusCell, sheet)
    //Obtain the initial cell of observation from properties file
    val initialCell = new CellReference(
        Configuration.getInitialCellSecondaryObservation)
    val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val actualRow = sheet.getRow(row)
      if(actualRow != null && !POIUtils.extractCellValue(
            actualRow.getCell(0), evaluator).trim().isEmpty()) {
//        var countryName : String = POIUtils.extractCellValue(
//            actualRow.getCell(0))
//        var country : Country = SpreadsheetsFetcher.obtainCountry(countryName)
        //We have to iterate throw the excel file
        for(column <- initialCell.getCol() to actualRow.getLastCellNum() - 1) {
        	//Obtain the indicator corresponds to the observation
            var indicator : Indicator = obtainIndicator(sheet, dataset, 
                column, row, initialCell)
            //Obtain the country corresponds to the observation 
            var country : Country = obtainCountry(sheet, dataset, column, row, 
                initialCell)
            //If country of the observation is null, there is no observation
            //for this cell
            if(country != null) {
              var year = dataset.year
    	      var value = POIUtils.extractNumericCellValue(actualRow.getCell(column), 
    	        evaluator)
    	      //Create the observation with the extracted data
    	      logger.info("Extracted observation of: " + dataset.id + " " +
    	        country.iso3Code + " " + indicator + " " + value)
    	      observations += createObservation(dataset, "", country, null, 
    	        indicator, year.toDouble, value, status)
            }    
//        	var year = POIUtils.extractCellValue(sheet.getRow(
//        	    initialCell.getRow() - 2).getCell(column), evaluator)

        }
      }
    }
    logger.info("Finish observations extraction")
    observations.toList
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
  def obtainCountry(sheet : Sheet, dataset : Dataset, column : Int, row : Int, 
      initialCell : CellReference) : Country = {
    var cell : Cell = null
    //We have to check if the countries are defined in a row or in a column
    if(dataset.isCountryInRow) 
      cell = sheet.getRow(initialCell.getRow() - 1).getCell(column)
    else
      cell = sheet.getRow(row).getCell(0)
    //Extract the name of the country
    val cellValue : String = POIUtils.extractCellValue(cell)  
    if(cellValue.isEmpty()) {
      //If the cell is empty, the method returns null
      null
    } else {
      //We ask to country reconciliator for the Web Index name of the country
      //extracted from the sheet
      var countryName : String = reconciliator.searchCountry(cellValue)
      logger.info("Obtaining country with name: " + countryName)
      //Ask to SpreadsheetFetcher for the country accord to the Web Index name
      SpreadsheetsFetcher.obtainCountry(countryName)
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
  def obtainIndicator(sheet : Sheet, dataset : Dataset, column : Int, 
      row : Int, initialCell : CellReference) : Indicator = {
    /*val cellReference = new CellReference(cell)
    val indicatorName = POIUtils.extractCellValue(
        sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))
    SpreadsheetsFetcher.obtainIndicator(indicatorName)*/
    var cell : Cell = null
    //We have to check if the indicators are defined in a row or in a column
    if(dataset.isCountryInRow) 
      cell = sheet.getRow(row).getCell(0)
    else
      cell = sheet.getRow(initialCell.getRow() - 1).getCell(column)
    //Extract indicator name
    val indicatorName : String = POIUtils.extractCellValue(cell)
    logger.info("Obtaining indicator from name: " + indicatorName)
    //Ask to SpreadsheetFetcher for the indicator with the name extracted
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
  def createObservation(dataset : Dataset, label : String, area : Area, 
      computation : Computation, indicator : Indicator, year : Double, 
      value : Double, status : String) : Observation = {
    var observation = new Observation
    observation.dataset = dataset
    observation.label = label
    observation.area = area
    observation.computation = computation
    observation.indicator = indicator
    observation.year = year.toInt
    if(value == -1) 
      observation.status = ObservationStatus.Missed    
    else {
       observation.status = status match {
         case "Raw" => ObservationStatus.Raw
         case "Imputed" => ObservationStatus.Imputed
         case "Normalised" => ObservationStatus.Normalised
         case "Missed" => ObservationStatus.Missed
         case "Sorted" => ObservationStatus.Sorted
         case "Adjusted" => ObservationStatus.Adjusted
         case "Weighted" => ObservationStatus.Weighted
         case _ => throw new IllegalArgumentException("Observation status " + 
             status + " is unknown")
       } 
       observation.value = value
    }
    observation
  }
  
  /**
   * This method has to extract the status of the observations
   * @param cell The cell where is the status
   * @param sheet The sheet that contains all observations of a dataset
   * @return The values extracted of the status cell
   */
  def obtainStatus(cell : String, sheet : Sheet) : String = {
    val cellReference : CellReference = new CellReference(cell)
    val stat = sheet.getRow(cellReference.getRow()).getCell(
        cellReference.getCol())
    if(stat == null)
      throw new IllegalArgumentException("Status cell is empty")
    POIUtils.extractCellValue(stat)
  }

}