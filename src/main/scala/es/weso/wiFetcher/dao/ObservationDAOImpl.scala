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

class ObservationDAOImpl(path : String, relativePath : Boolean) 
	extends ObservationDAO {
  
  val countries : List[Country] = new CountryDAOImpl(
      Configuration.getCountryFile, true).getCountries
  
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
    val indicator = null//obtainIndicator(Configuration.getIndicatorCell, sheet)
    val status = null//obtainStatus(Configuration.getStatusCell, sheet)
    val initialCell = new CellReference(
        Configuration.getInitialCellSecondaryObservation)
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val actualRow = sheet.getRow(row)
      if(actualRow != null && !POIUtils.extractCellValue(
            actualRow.getCell(0)).trim().isEmpty()) {
        var countryName : String = POIUtils.extractCellValue(
            actualRow.getCell(0))
        var country : Country = null//obtainCountry(countryName)
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

}