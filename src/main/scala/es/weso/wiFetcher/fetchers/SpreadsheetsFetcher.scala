package es.weso.wiFetcher.fetchers

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.utils.POIUtils
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.IndicatorType
import org.apache.poi.ss.usermodel.Sheet
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.dao.CountryDAOImpl
import es.weso.wiFetcher.utils.FileUtils

class SpreadsheetsFetcher extends Fetcher {
	 
  var file : InputStream = null
  var workbook : Workbook = null
  var countries : List[Country] = new CountryDAOImpl(
      Configuration.getCountryFile, true).getCountries
  
  def loadWorkbook(path : String, relativePath : Boolean = false) = {
    val currentFile = new File(FileUtils.getFilePath(path, relativePath))
    file = new FileInputStream(currentFile)
    workbook = WorkbookFactory.create(file)
  }
  
  def getObservations(datasets : List[Dataset]) : List[Observation] = {
    if(datasets == null)
      throw new IllegalArgumentException("List of datasets to load their " +
      		"observations is null")
    var observations = ListBuffer[Observation]()
    for(dataset <- datasets)
      observations.insertAll(0, extractObservationsByDataset(dataset))
    observations.toList
  }
  
  def extractObservationsByDataset(dataset: Dataset) : List[Observation] = {
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
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val actualRow = sheet.getRow(row)
      if(actualRow != null && !POIUtils.extractCellValue(
            actualRow.getCell(0)).trim().isEmpty()) {
        var countryName : String = POIUtils.extractCellValue(
            actualRow.getCell(0))
        var country : Country = obtainCountry(countryName)
        for(column <- initialCell.getCol() to actualRow.getLastCellNum() - 1) {
        	var year = POIUtils.extractCellValue(sheet.getRow(
        	    initialCell.getRow() - 2).getCell(column))
    	    var value = POIUtils.extractCellValue(actualRow.getCell(column))
    	    //TODO Create the observation with all data. Dataset, label, 
    	    //Area, Computation, Inidicator and status (Raw, Imputed, 
    	    //Normalised) in this order
        	if(!value.trim.isEmpty()) {
        		val y = year.toDouble
				observations += new Observation(dataset, "", country, null, 
				    indicator, y.toInt, value.toDouble, status)
        	}
        }
      }
    }
    observations.toList
  } 
  
  def obtainCountry(countryName : String) : Country = {
    if(countryName == null || countryName.isEmpty()) 
      throw new IllegalArgumentException("The name of the country cannot " +
      		"be null o empty")
    countries.find(c => c.name.equals(countryName)).getOrElse(
        throw new IllegalArgumentException("Not exist country with name " + 
            countryName))
  }
  
  def obtainIndicator(indicatorCell : String, sheet : Sheet) : Indicator = {
    if(indicatorCell == null || sheet == null)
      throw new IllegalArgumentException("Cell specifies for the " +
      		"indicator name is not correct")
    //TODO This method has to extract the name of the indicator of the 
    //sprearsheets and find or create the object that represents this indicator
    val cell = new CellReference(indicatorCell)
    val indicator = new Indicator(IndicatorType.Secondary, 
        POIUtils.extractCellValue(sheet.getRow(cell.getRow())
            .getCell(cell.getCol())), "", null, null, 62)
    indicator
  }
  
  def obtainStatus(statusCell : String, sheet : Sheet) : String = {
    if(statusCell == null || sheet == null)
      throw new IllegalArgumentException("Cell specifies for the status of a " +
      		"indicator is not correct")
    val cell = new CellReference(statusCell)
    POIUtils.extractCellValue(sheet.getRow(cell.getRow())
        .getCell(cell.getCol()))
  }
  
}