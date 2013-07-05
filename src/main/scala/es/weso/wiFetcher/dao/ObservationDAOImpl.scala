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

class ObservationDAOImpl(path : String, relativePath : Boolean) 
	extends ObservationDAO {
  
  var workbook : Workbook = WorkbookFactory.create(new FileInputStream(
      new File(FileUtils.getFilePath(path, relativePath))))
  
  def getObservations(datasets : List[Dataset]) : List[Observation] = {
    if(datasets == null)
      throw new IllegalArgumentException("List of datasets to load their " +
      		"observations is null")
    var observations = ListBuffer[Observation]()
    for(dataset <- datasets)
      observations.insertAll(0, getObservationsByIndicator(dataset))
    observations.toList
  }
  
  def getObservationsByIndicator(dataset : Dataset) : List[Observation] = {
    if(dataset == null)
      throw new IllegalArgumentException("Cannot extract observations of a " +
      		"null dataset")
    var observations = ListBuffer[Observation]()
    val sheet = workbook.getSheet(dataset.id)
    if(sheet == null) 
      throw new IllegalArgumentException("There isn't data for dataset: " + 
          dataset.id)
    val indicator = obtainIndicator(Configuration.getIndicatorCell, sheet)
    val status = obtainStatus(Configuration.getStatusCell, sheet)
    val initialCell = new CellReference(
        Configuration.getInitialCellSecondaryObservation)
    val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val actualRow = sheet.getRow(row)
      if(actualRow != null && !POIUtils.extractCellValue(
            actualRow.getCell(0), evaluator).trim().isEmpty()) {
        var countryName : String = POIUtils.extractCellValue(
            actualRow.getCell(0))
        var country : Country = SpreadsheetsFetcher.obtainCountry(countryName)
        for(column <- initialCell.getCol() to actualRow.getLastCellNum() - 1) {
        	var year = POIUtils.extractCellValue(sheet.getRow(
        	    initialCell.getRow() - 2).getCell(column), evaluator)
    	    var value = POIUtils.extractCellValue(actualRow.getCell(column), 
    	        evaluator)
    	        //TODO Create the observation with all data. Dataset, label, 
    	    //Area, Computation, Inidicator and status (Raw, Imputed, 
    	    //Normalised) in this order
    	    observations += createObservation(dataset, "", country, null, 
    	        indicator, year.toDouble, value, status)

        }
      }
    }
    observations.toList
  }
  
  def obtainIndicator(cell : String, sheet : Sheet) : Indicator = {
    val cellReference = new CellReference(cell)
    val indicatorName = POIUtils.extractCellValue(
        sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))
    SpreadsheetsFetcher.obtainIndicator(indicatorName)
  }
  
  def createObservation(dataset : Dataset, label : String, area : Area, 
      computation : Computation, indicator : Indicator, year : Double, 
      value : String, status : String) : Observation = {
    var observation = new Observation
    observation.dataset = dataset
    observation.label = label
    observation.area = area
    observation.computation = computation
    observation.indicator = indicator
    observation.year = year.toInt
    if(value.trim.isEmpty()) 
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
       observation.value = value.toDouble
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