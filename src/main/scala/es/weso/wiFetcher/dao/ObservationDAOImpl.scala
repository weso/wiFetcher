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

class ObservationDAOImpl(path : String, relativePath : Boolean) 
	extends ObservationDAO {
  
  var workbook : Workbook = WorkbookFactory.create(new FileInputStream(
      new File(FileUtils.getFilePath(path, relativePath))))
      
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  /**
   *  This method loads all observations for each dataset that receive in a list
   */
  def getObservations(datasets : List[Dataset]) : List[Observation] = {
    if(datasets == null){
      logger.error("List of datasets to load their " +
      		"observations is null")
      throw new IllegalArgumentException("List of datasets to load their " +
      		"observations is null")
    }
    var observations = ListBuffer[Observation]()
    for(dataset <- datasets)
      observations.insertAll(0, getObservationsByDataset(dataset))
    observations.toList
  }
  
  /**
   * This method loads all observations 
   */
  def getObservationsByDataset(dataset : Dataset) : List[Observation] = {
    if(dataset == null) {
      logger.error("Cannot extract observations of a " +
      		"null dataset")
      throw new IllegalArgumentException("Cannot extract observations of a " +
      		"null dataset")
    }
    var observations = ListBuffer[Observation]()
    val sheet = workbook.getSheet(dataset.id)
    if(sheet == null) {
      logger.error("There isn't data for dataset: " + 
          dataset.id)
      throw new IllegalArgumentException("There isn't data for dataset: " + 
          dataset.id)
    }     
    logger.info("Begin observations extraction")
    //val indicator = obtainIndicator(Configuration.getIndicatorCell, sheet)
    //Status of observations depends on excel templates
    val status = "Raw"/*obtainStatus(Configuration.getStatusCell, sheet)*/
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
        for(column <- initialCell.getCol() to actualRow.getLastCellNum() - 1) {
            var indicator : Indicator = obtainIndicator(sheet, dataset, 
                column, row, initialCell)
            var country : Country = obtainCountry(sheet, dataset, column, row, 
                initialCell)
            //If country of the observation is null, there is no observation
            //for this cell
            if(country != null) {
              var year = dataset.year
    	      var value = POIUtils.extractNumericCellValue(actualRow.getCell(column), 
    	        evaluator)
    	      //TODO Create the observation with all data. Dataset, label, 
    	      //Area, Computation, Inidicator and status (Raw, Imputed, 
    	      //Normalised) in this order
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
  
  def obtainCountry(sheet : Sheet, dataset : Dataset, column : Int, row : Int, 
      initialCell : CellReference) : Country = {
    var cell : Cell = null
    if(dataset.isCountryInRow) 
      cell = sheet.getRow(initialCell.getRow() - 1).getCell(column)
    else
      cell = sheet.getRow(row).getCell(0)
    val cellValue : String = POIUtils.extractCellValue(cell)  
    if(cellValue.isEmpty()) {
      null
    } else {
      var countryName : String = reconciliator.searchCountry(cellValue)
      logger.info("Obtaining country with name: " + countryName)    
      SpreadsheetsFetcher.obtainCountry(countryName)
    }
  }
  
  def obtainIndicator(sheet : Sheet, dataset : Dataset, column : Int, 
      row : Int, initialCell : CellReference) : Indicator = {
    /*val cellReference = new CellReference(cell)
    val indicatorName = POIUtils.extractCellValue(
        sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))
    SpreadsheetsFetcher.obtainIndicator(indicatorName)*/
    var cell : Cell = null
    if(dataset.isCountryInRow) 
      cell = sheet.getRow(row).getCell(0)
    else
      cell = sheet.getRow(initialCell.getRow() - 1).getCell(column)
    val indicatorName : String = POIUtils.extractCellValue(cell)
    logger.info("Obtaining indicator from name: " + indicatorName)
    SpreadsheetsFetcher.obtainIndicator(indicatorName)
  }
  
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
         case _ => throw new IllegalArgumentException("Observation status " + 
             status + " is unknown")
       } 
       observation.value = value
    }
    observation
  }
  
  def obtainStatus(cell : String, sheet : Sheet) : String = {
    val cellReference : CellReference = new CellReference(cell)
    val stat = sheet.getRow(cellReference.getRow()).getCell(
        cellReference.getCol())
    if(stat == null)
      throw new IllegalArgumentException("Status cell is empty")
    POIUtils.extractCellValue(stat)
  }

}